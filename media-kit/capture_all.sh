#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHOTS="$DIR/screenshots"
VIDEOS="$DIR/videos"

mkdir -p "$SHOTS" "$VIDEOS"

echo "=== 1. Capturando Screenshots das Telas Principais e Modais ==="

# Garante foco no Launcher
adb shell am start -n com.decklauncher/.MainActivity
sleep 1.5

# 01 - Home Jogos
adb shell screencap -p /sdcard/01_home_jogos.png
adb pull /sdcard/01_home_jogos.png "$SHOTS/01_home_jogos.png"

# R1 - Ir para Emuladores
adb shell input keyevent 103 # KEYCODE_BUTTON_R1
sleep 0.8
adb shell screencap -p /sdcard/02_aba_emuladores.png
adb pull /sdcard/02_aba_emuladores.png "$SHOTS/02_aba_emuladores.png"

# R1 - Ir para Apps
adb shell input keyevent 103 # KEYCODE_BUTTON_R1
sleep 0.8
adb shell screencap -p /sdcard/03_aba_apps.png
adb pull /sdcard/03_aba_apps.png "$SHOTS/03_aba_apps.png"

# Entrar na pasta de apps (DPAD_CENTER)
adb shell input keyevent 23 # DPAD_CENTER
sleep 0.8
adb shell screencap -p /sdcard/04_dentro_pasta_apps.png
adb pull /sdcard/04_dentro_pasta_apps.png "$SHOTS/04_dentro_pasta_apps.png"

# Voltar da pasta
adb shell input keyevent 4 # BACK
sleep 0.5

# Voltar para Jogos (L1 duas vezes)
adb shell input keyevent 102 # KEYCODE_BUTTON_L1
sleep 0.5
adb shell input keyevent 102 # KEYCODE_BUTTON_L1
sleep 0.5

# Abrir Menu de Opções / ItemMenu (Botão X / Menu)
adb shell input keyevent 99 # KEYCODE_BUTTON_X
sleep 1.0
adb shell screencap -p /sdcard/05_drawer_opcoes_jogo.png
adb pull /sdcard/05_drawer_opcoes_jogo.png "$SHOTS/05_drawer_opcoes_jogo.png"

# Descer para Personalizar Metadados e selecionar
adb shell input keyevent 20 # DPAD_DOWN
sleep 0.4
adb shell input keyevent 96 # BUTTON_A
sleep 1.2
adb shell screencap -p /sdcard/06_drawer_personalizar_metadados.png
adb pull /sdcard/06_drawer_personalizar_metadados.png "$SHOTS/06_drawer_personalizar_metadados.png"

# Fechar metadados
adb shell input keyevent 97 # BUTTON_B
sleep 0.8

# Subir até a TopBar e abrir Configurações do Launcher (StartMenu)
adb shell input keyevent 19 # DPAD_UP
sleep 0.5
adb shell input keyevent 96 # BUTTON_A (ou clicar no avatar)
sleep 1.2
adb shell screencap -p /sdcard/07_start_menu_biblioteca.png
adb pull /sdcard/07_start_menu_biblioteca.png "$SHOTS/07_start_menu_biblioteca.png"

# Alternar para aba Aparência
adb shell input keyevent 22 # DPAD_RIGHT
sleep 0.6
adb shell screencap -p /sdcard/08_start_menu_aparencia.png
adb pull /sdcard/08_start_menu_aparencia.png "$SHOTS/08_start_menu_aparencia.png"

# Alternar para aba Bancos de Dados
adb shell input keyevent 22 # DPAD_RIGHT
sleep 0.6
adb shell screencap -p /sdcard/09_start_menu_bancos_dados.png
adb pull /sdcard/09_start_menu_bancos_dados.png "$SHOTS/09_start_menu_bancos_dados.png"

# Fechar StartMenu
adb shell input keyevent 97 # BUTTON_B
sleep 0.8

echo "=== 2. Gravando Tour de Navegação pela Interface (20 segundos) ==="
adb shell screenrecord --time-limit 20 --bit-rate 12000000 /sdcard/tour_interface.mp4 &
REC_PID=$!

sleep 1.5
# Navega pelos cards de jogos
for i in {1..3}; do
    adb shell input keyevent 22 # DPAD_RIGHT
    sleep 0.7
done
for i in {1..2}; do
    adb shell input keyevent 21 # DPAD_LEFT
    sleep 0.7
done

# Troca para Emuladores
adb shell input keyevent 103 # R1
sleep 1.2
adb shell input keyevent 22 # DPAD_RIGHT
sleep 0.7
adb shell input keyevent 22 # DPAD_RIGHT
sleep 0.7

# Troca para Apps
adb shell input keyevent 103 # R1
sleep 1.2
adb shell input keyevent 23 # Entra na pasta
sleep 1.2
adb shell input keyevent 22 # Passa app 1
sleep 0.8
adb shell input keyevent 22 # Passa app 2
sleep 0.8
adb shell input keyevent 4 # Volta da pasta
sleep 1.0

# Volta para Jogos
adb shell input keyevent 102 # L1
sleep 0.6
adb shell input keyevent 102 # L1
sleep 0.8

wait $REC_PID || true
sleep 1
adb pull /sdcard/tour_interface.mp4 "$VIDEOS/01_tour_interface.mp4"

echo "=== Screenshots e Tour de Interface gravados com sucesso! ==="
