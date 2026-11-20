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

[ "$fail" -eq 0 ] && echo "✅ check-rules OK" || { echo "Corrija os itens acima."; exit 1; }
