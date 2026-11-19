import os
import sys
import time
import subprocess

DEVICE = "RX2X700QMPP"

def run_adb(cmd):
    full_cmd = ["adb", "-s", DEVICE] + cmd
    return subprocess.run(full_cmd, capture_output=True, text=True)

def record_scene(output_file, duration, action_fn):
    print(f"\n--- Gravando {output_file} ({duration}s) ---")
    remote_path = f"/sdcard/{os.path.basename(output_file)}"
    run_adb(["shell", f"rm -f {remote_path}"])

    # Inicia gravação em segundo plano em Full HD 1920x1080
    rec_proc = subprocess.Popen([
        "adb", "-s", DEVICE, "shell",
        "screenrecord", "--size", "1920x1080", "--bit-rate", "16000000",
        "--time-limit", str(duration + 4), remote_path
    ])

    time.sleep(1.0) # estabiliza gravação
    action_fn()
    time.sleep(1.0)

    # Finaliza gravação via SIGINT para fechar o arquivo MP4 perfeitamente
    run_adb(["shell", "kill -2 $(pidof screenrecord)"])
    rec_proc.wait(timeout=5)
    time.sleep(1.0)

    # Faz o pull do arquivo gravado
    print(f"Baixando {output_file}...")
    run_adb(["pull", remote_path, output_file])
    print(f"Pronto: {output_file} ({os.path.getsize(output_file) / 1024 / 1024:.2f} MB)")

def setup_launcher():
    run_adb(["shell", "am start -n com.decklauncher/.MainActivity"])
    time.sleep(1.0)
    # Fecha qualquer diálogo aberto
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.3)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.3)

def scene1_actions():
    setup_launcher()
    # Aba Jogos
    run_adb(["shell", "input tap 830 60"])
    time.sleep(0.8)
    # Volta para o início
    for _ in range(10):
        run_adb(["shell", "input keyevent KEYCODE_DPAD_LEFT"])
        time.sleep(0.08)
    time.sleep(1.5)

    # Navega suavemente pelos jogos
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.6)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.6)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.6)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.6)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_LEFT"])
    time.sleep(1.5)

def scene2_actions():
    setup_launcher()
    # Aba Emuladores
    run_adb(["shell", "input tap 1000 60"])
    time.sleep(0.8)
    # Navega até o Dolphin Emulator
    for _ in range(5):
        run_adb(["shell", "input keyevent KEYCODE_DPAD_LEFT"])
        time.sleep(0.08)
    time.sleep(0.5)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(0.4)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.0)

    # Inicia Dolphin: Boot de console (GameCube ou Wii)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_A"])
    time.sleep(4.5) # Aguarda o boot com som e transição
    setup_launcher()
    time.sleep(1.2)

    # Aba Jogos: Inicia Cooking Mama (NDS Boot)
    run_adb(["shell", "input tap 830 60"])
    time.sleep(0.6)
    for _ in range(8):
        run_adb(["shell", "input keyevent KEYCODE_DPAD_LEFT"])
        time.sleep(0.08)
    time.sleep(0.8)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_A"])
    time.sleep(2.5) # Deixa a intro do DS tocar
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"]) # Pula suave
    time.sleep(1.0)
    setup_launcher()

def scene3_actions():
    setup_launcher()
    # Aba Emuladores
    run_adb(["shell", "input tap 1000 60"])
    time.sleep(1.0)
    # Navega pelos emuladores
    for _ in range(4):
        run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
        time.sleep(1.0)

    # Aba Apps
    run_adb(["shell", "input tap 1160 60"])
    time.sleep(1.2)
    # Abre uma pasta (Mídia & Streaming ou Ferramentas)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_A"])
    time.sleep(2.0)
    # Retorna da pasta
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(1.5)

def scene4_actions():
    setup_launcher()
    # Aba Jogos
    run_adb(["shell", "input tap 830 60"])
    time.sleep(0.8)
    # Abre Opções (X)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_X"])
    time.sleep(1.5)

    # Editar Logo 3D
    run_adb(["shell", "input tap 900 625"])
    time.sleep(2.5)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.8)

    # Trocar Intro do Console
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_X"])
    time.sleep(0.8)
    run_adb(["shell", "input tap 900 715"])
    time.sleep(2.5)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.5)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.8)

    # Configurações do Launcher (Avatar)
    run_adb(["shell", "input tap 120 60"])
    time.sleep(1.5)
    # Scroll suave para baixo
    run_adb(["shell", "input swipe 1000 800 1000 300 400"])
    time.sleep(2.5)
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(1.0)

def scene5_actions():
    setup_launcher()
    time.sleep(0.5)
    # Abre Configurações do Launcher (Avatar)
    run_adb(["shell", "input tap 120 60"])
    time.sleep(1.2)
    # Rola até a seção de temas e cores
    run_adb(["shell", "input swipe 1000 900 1000 350 350"])
    time.sleep(1.0)
    # Alterna para Vermelho Nintendo (Cyber Red)
    run_adb(["shell", "input tap 810 485"])
    time.sleep(1.0)
    # Alterna para Verde Matrix
    run_adb(["shell", "input tap 900 485"])
    time.sleep(1.0)
    # Volta para Vermelho Nintendo
    run_adb(["shell", "input tap 810 485"])
    time.sleep(1.0)
    # Testa Modo de Fundo Sóbrio (Estilo console)
    run_adb(["shell", "input tap 1240 390"])
    time.sleep(1.2)
    # Volta para Modo Imersivo
    run_adb(["shell", "input tap 760 390"])
    time.sleep(1.2)
    # Fecha o diálogo de configurações
    run_adb(["shell", "input keyevent KEYCODE_BUTTON_B"])
    time.sleep(0.8)
    # Navega para exibir o foco e contorno em vermelho
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.0)
    run_adb(["shell", "input keyevent KEYCODE_DPAD_RIGHT"])
    time.sleep(1.2)

if __name__ == "__main__":
    out_dir = "/home/tiago/dev/projetos/deck-launcher/marketing"
    os.makedirs(out_dir, exist_ok=True)

    record_scene(f"{out_dir}/scene5_themes.mp4", 14, scene5_actions)

