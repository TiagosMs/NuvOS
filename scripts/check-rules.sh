#!/usr/bin/env bash
# Falha se encontrar padrões proibidos. Para exceção justificada, adicione "// rule-ok: motivo" na linha.
set -u
fail=0
check() {  # $1=descrição  $2=regex  $3=caminho
  hits=$(grep -rnE "$2" $3 2>/dev/null | grep -v "rule-ok")
  if [ -n "$hits" ]; then echo "❌ $1"; echo "$hits"; echo; fail=1; fi
}

SRC="app/src/main"
check "blur ao vivo / blur de janela"       '\.blur\(|RenderEffect|FLAG_BLUR_BEHIND'         "$SRC/java"
check "GlobalScope / runBlocking"            'GlobalScope|runBlocking'                        "$SRC/java"
check "CoroutineScope solto"                 'CoroutineScope\(Dispatchers'                    "$SRC/java"
check "HTTP sem TLS"                         'http://'                                        "$SRC/java"
check "cleartext/backup no manifest"         'usesCleartextTraffic="true"|allowBackup="true"' "$SRC/AndroidManifest.xml"
check "icons-extended"                       'material-icons-extended'                        "app/build.gradle.kts"
check "permissão de áudio sem uso"           'RECORD_AUDIO'                                   "$SRC/AndroidManifest.xml"
check "possível segredo hardcoded"           '(api[_-]?key|secret|token|password)[^=\n]{0,20}=\s*"[A-Za-z0-9_\-]{20,}"' "$SRC/java"
check "Log de dados sensíveis"               'Log\.[dvi]\(.*(key|token|senha|password)'      "$SRC/java"
check "mutableMapOf como cache"              'memoryCache\s*=\s*mutableMapOf'                 "$SRC/java"

# Regras Automáticas v1.1 (T8.2)
check "Pacote de ícones proibido no código"  'iconpack'                                       "app/src"
check "Better xCloud fixo proibido"          'betterxc'                                       "app/src"
check "Uri.fromFile não permitido"           'Uri\.fromFile'                                  "$SRC/java"
check "delete recursivo fora de RomOrganizer" '\.deleteRecursively\(\)'                       "$SRC/java/com/decklauncher/data/RomScanner.kt"

[ "$fail" -eq 0 ] && echo "✅ check-rules OK" || { echo "Corrija os itens acima."; exit 1; }
