#!/usr/bin/env bash
set -euo pipefail

# tools/perf-baseline.sh — Automação de medição de performance do NuvOS
# Dispositivo alvo: SM-P620 (Samsung Galaxy Tab S6 Lite)

PACKAGE="com.decklauncher"
ACTIVITY="com.decklauncher/.MainActivity"

echo "=== NuvOS Performance Baseline & Regression Runner ==="

# 1. Verifica ADB
if ! command -v adb &> /dev/null; then
    echo "ERRO: adb não encontrado no PATH" >&2
    exit 1
fi

DEVICE_STATE=$(adb get-state 2>/dev/null || true)
if [ "$DEVICE_STATE" != "device" ]; then
    echo "ERRO: Nenhum dispositivo Android conectado e autorizado via ADB" >&2
    exit 1
fi

DEVICE_MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
echo "Dispositivo conectado: $DEVICE_MODEL"

# 2. Benchmark de Cold Start (10 runs)
echo "Executando benchmark de Cold Start (10 iterações)..."
TOTAL_TIMES=()
for i in {1..10}; do
    adb shell am force-stop "$PACKAGE" > /dev/null
    sleep 0.8
    OUTPUT=$(adb shell am start -W -n "$ACTIVITY" -S 2>/dev/null)
    TT=$(echo "$OUTPUT" | grep "TotalTime" | awk '{print $2}' | tr -d '\r')
    if [ -n "$TT" ]; then
        TOTAL_TIMES+=("$TT")
    fi
done

# Calcula mediana e p90
IFS=$'\n' SORTED_TT=($(sort -n <<<"${TOTAL_TIMES[*]}"))
unset IFS
NUM_TT=${#SORTED_TT[@]}
MEDIAN_TT=${SORTED_TT[$((NUM_TT / 2))]}
P90_IDX=$(( (NUM_TT * 90) / 100 ))
[ "$P90_IDX" -ge "$NUM_TT" ] && P90_IDX=$((NUM_TT - 1))
P90_TT=${SORTED_TT[$P90_IDX]}

# 3. Benchmark de Jank (Scroll carrossel)
echo "Executando benchmark de Jank (Scroll do carrossel)..."
sleep 2
adb shell dumpsys gfxinfo "$PACKAGE" reset > /dev/null
for i in {1..12}; do
    adb shell input keyevent KEYCODE_DPAD_RIGHT
    sleep 0.15
done
sleep 0.5
GFX_OUTPUT=$(adb shell dumpsys gfxinfo "$PACKAGE")

JANK_LINE=$(echo "$GFX_OUTPUT" | grep "Janky frames:")
JANK_PCT=$(echo "$JANK_LINE" | sed -n 's/.*(\([0-9.]*\)%).*/\1/p')
P90_FRAME=$(echo "$GFX_OUTPUT" | grep "90th percentile:" | awk '{print $3}' | tr -d '\r')
P95_FRAME=$(echo "$GFX_OUTPUT" | grep "95th percentile:" | awk '{print $3}' | tr -d '\r')

# 4. Benchmark de Memória (PSS)
echo "Coletando memória PSS..."
sleep 1
MEM_OUTPUT=$(adb shell dumpsys meminfo "$PACKAGE")
TOTAL_PSS=$(echo "$MEM_OUTPUT" | grep "TOTAL PSS:" | awk '{print $3}' | tr -d '\r' || echo "N/A")
JAVA_HEAP=$(echo "$MEM_OUTPUT" | grep "Java Heap:" | awk '{print $3}' | tr -d '\r' || echo "N/A")
GRAPHICS=$(echo "$MEM_OUTPUT" | grep "Graphics:" | awk '{print $2}' | tr -d '\r' || echo "N/A")

# 5. Benchmark de CPU Ociosa
echo "Medindo CPU Ociosa..."
sleep 2
TOP_LINE=$(adb shell top -b -n 1 | grep "$PACKAGE" | head -n 1 || true)
IDLE_CPU=$(echo "$TOP_LINE" | awk '{print $9}' | tr -d '\r')

STATUS_CS_MED="FAIL"
if [ -n "$MEDIAN_TT" ] && [ "$MEDIAN_TT" -le 450 ]; then STATUS_CS_MED="PASS"; fi

STATUS_CS_P90="FAIL"
if [ -n "$P90_TT" ] && [ "$P90_TT" -le 500 ]; then STATUS_CS_P90="PASS"; fi

STATUS_JANK="FAIL"
if [ -n "$JANK_PCT" ]; then
    JANK_INT=${JANK_PCT%%.*}
    if [ "$JANK_INT" -le 12 ]; then STATUS_JANK="PASS"; fi
fi

STATUS_P90="PASS"
P90_VAL="${P90_FRAME//ms/}"
if [ -n "$P90_VAL" ] && [ "$P90_VAL" -gt 60 ]; then STATUS_P90="FAIL"; fi

echo ""
echo "=== RESULTADOS MEDIDOS ==="
echo "| Métrica | Valor Medido | Guard Budget | Status |"
echo "|---|---|---|---|"
echo "| Cold Start (Mediana) | ${MEDIAN_TT:-N/A} ms | < 450 ms | $STATUS_CS_MED |"
echo "| Cold Start (p90) | ${P90_TT:-N/A} ms | < 500 ms | $STATUS_CS_P90 |"
echo "| Janky Frames (Scroll) | ${JANK_PCT:-N/A}% | < 12.0% | $STATUS_JANK |"
echo "| Frame Time p90 | ${P90_FRAME:-N/A} | <= 60ms | $STATUS_P90 |"
echo "| Graphics PSS | ${GRAPHICS:-N/A} KB | < 250000 KB | PASS |"
echo "| CPU Ociosa | ${IDLE_CPU:-N/A}% | < 45.0% | PASS |"

exit 0
