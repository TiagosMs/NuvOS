import os
import sys
import shutil
import subprocess

FFMPEG = "/home/tiago/.local/bin/ffmpeg"
BASE_DIR = "/home/tiago/dev/projetos/deck-launcher"
MKT_DIR = os.path.join(BASE_DIR, "marketing")
ASSETS_DIR = os.path.join(BASE_DIR, "app/src/main/assets/intros")
TMP_DIR = "/tmp/trailer_build_nintendo"

FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_NORM = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"

def run_cmd(cmd):
    print(f"Executando: {' '.join(cmd[:6])}...")
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"ERRO: {res.stderr}")
        sys.exit(1)
    return res

def make_nintendo_card(tag, line1, line2, accent_color="0xe60012"):
    """
    Lower-third limpo, sem slop:
    Inspirado no visual Nintendo Switch OLED:
    - Base em grafite escuro com transparência elegante (0x16191d@0.90)
    - Borda fina e discreta (0x323842@0.7)
    - Pílula Joy-Con de destaque (vermelha #e60012 ou azul #00c3e3)
    - Tipografia hierárquica em caixa mista, sem excessos
    """
    def esc(txt):
        return txt.replace(":", "\\:").replace("'", "\\'")

    return (
        f"drawbox=x=70:y=885:w=1780:h=135:color=0x16191d@0.90:t=fill,"
        f"drawbox=x=70:y=885:w=1780:h=135:color=0x323842@0.7:t=1,"
        f"drawbox=x=70:y=885:w=6:h=135:color={accent_color}:t=fill,"
        f"drawtext=fontfile={FONT_BOLD}:text='{esc(tag)}':fontcolor={accent_color}:fontsize=20:x=105:y=905,"
        f"drawtext=fontfile={FONT_BOLD}:text='{esc(line1)}':fontcolor=white:fontsize=32:x=105:y=938,"
        f"drawtext=fontfile={FONT_NORM}:text='{esc(line2)}':fontcolor=0xa8afb8:fontsize=23:x=105:y=978"
    )

def build_trailer():
    if os.path.exists(TMP_DIR):
        shutil.rmtree(TMP_DIR)
    os.makedirs(TMP_DIR, exist_ok=True)

    print("=== UNSLOP: RENDERIZANDO TRAILER COM ESTÉTICA NINTENDO ===")

    # 1. Clip 0: Abertura Nintendo Switch Style (3.0s)
    # 0.0s - 3.0s
    print("1/7: Renderizando Clip 0 (Abertura Nintendo Switch Style)...")
    c0_path = os.path.join(TMP_DIR, "clip_0.mp4")
    vf_intro = (
        f"drawbox=x=0:y=0:w=8:h=1080:color=0xe60012:t=fill,"
        f"drawbox=x=1912:y=0:w=8:h=1080:color=0x00c3e3:t=fill,"
        f"drawtext=fontfile={FONT_BOLD}:text='NINTENDO-INSPIRED LAUNCHER':fontcolor=0xe60012:fontsize=22:x=(w-text_w)/2:y=340,"
        f"drawtext=fontfile={FONT_BOLD}:text='DECK LAUNCHER':fontcolor=white:fontsize=76:x=(w-text_w)/2:y=385,"
        f"drawtext=fontfile={FONT_NORM}:text='A Experiência Portátil Definitiva para Android':fontcolor=0x9ea6b0:fontsize=30:x=(w-text_w)/2:y=490,"
        f"drawbox=x=(w-700)/2:y=590:w=700:h=50:color=0x20242b@0.9:t=fill,"
        f"drawbox=x=(w-700)/2:y=590:w=700:h=50:color=0x383e4a@0.7:t=1,"
        f"drawtext=fontfile={FONT_BOLD}:text='Jogos   •   Emuladores   •   Nuvem   •   60 FPS':fontcolor=white:fontsize=21:x=(w-text_w)/2:y=605,"
        f"fade=t=in:st=0:d=0.4,fade=t=out:st=2.7:d=0.3"
    )
    run_cmd([
        FFMPEG, "-f", "lavfi", "-i", "color=c=0x14171c:s=1920x1080:r=60:d=3.0",
        "-vf", vf_intro, "-c:v", "libx264", "-preset", "fast", "-crf", "18",
        "-pix_fmt", "yuv420p", c0_path, "-y"
    ])

    # 2. Clip 1: Hero & Logo 3D 3DS (8.5s)
    # 3.0s - 11.5s
    print("2/7: Renderizando Clip 1 (Hero & Logo 3D 3DS)...")
    c1_path = os.path.join(TMP_DIR, "clip_1.mp4")
    lt1 = make_nintendo_card(
        tag="●  PERSPECTIVA NINTENDO 3DS",
        line1="Logo 3D Flutuante & Hero 16:9 Imersivo",
        line2="Profundidade com sombras estilo 3DS e arte dinâmica SteamGridDB",
        accent_color="0xe60012"
    )
    vf1 = f"fps=60,scale=1920:1080,{lt1}"
    run_cmd([
        FFMPEG, "-ss", "2.5", "-i", os.path.join(MKT_DIR, "scene1_hero.mp4"),
        "-t", "8.5", "-vf", vf1, "-c:v", "libx264", "-preset", "fast",
        "-crf", "18", "-pix_fmt", "yuv420p", c1_path, "-y"
    ])

    # 3. Clip 2A: Dolphin + GameCube Boot (5.5s)
    # 11.5s - 17.0s (Audio toca em 14.0s)
    print("3/7: Renderizando Clip 2A (Dolphin + GameCube Boot)...")
    c2a_path = os.path.join(TMP_DIR, "clip_2a.mp4")
    lt2a = make_nintendo_card(
        tag="●  NINTENDO GAMECUBE & WII",
        line1="Boot Intros dos Consoles com Áudio Oficial",
        line2="Transição suave Fade to Black • Dolphin alternando GC e Wii",
        accent_color="0xe60012"
    )
    vf2a = f"fps=60,scale=1920:1080,{lt2a}"
    run_cmd([
        FFMPEG, "-ss", "5.0", "-i", os.path.join(MKT_DIR, "scene2_boot.mp4"),
        "-t", "5.5", "-vf", vf2a, "-c:v", "libx264", "-preset", "fast",
        "-crf", "18", "-pix_fmt", "yuv420p", c2a_path, "-y"
    ])

    # 4. Clip 2B: Cooking Mama + NDS Boot (4.5s)
    # 17.0s - 21.5s (Audio toca em 19.5s)
    print("4/7: Renderizando Clip 2B (Cooking Mama + NDS Boot)...")
    c2b_path = os.path.join(TMP_DIR, "clip_2b.mp4")
    lt2b = make_nintendo_card(
        tag="●  NINTENDO DS & EXECUÇÃO NATIVA",
        line1="Lançamento Direto de ROMs & Emuladores",
        line2="Nintendo DS, 3DS, PSP, PS1, PS2, Switch, Wii e Xbox",
        accent_color="0xe60012"
    )
    vf2b = f"fps=60,scale=1920:1080,{lt2b}"
    run_cmd([
        FFMPEG, "-ss", "12.5", "-i", os.path.join(MKT_DIR, "scene2_boot.mp4"),
        "-t", "4.5", "-vf", vf2b, "-c:v", "libx264", "-preset", "fast",
        "-crf", "18", "-pix_fmt", "yuv420p", c2b_path, "-y"
    ])

    # 5. Clip 3: Personalização de Temas & Cores (10.0s)
    # 21.5s - 31.5s
    print("5/7: Renderizando Clip 3 (Personalização de Temas & Cores)...")
    c3_path = os.path.join(TMP_DIR, "clip_3.mp4")
    lt3 = make_nintendo_card(
        tag="●  PERSONALIZAÇÃO & TEMAS",
        line1="Cores de Destaque & Modos de Fundo",
        line2="Tema Vermelho Nintendo, Verde ou Ciano • Modos Sóbrio e Imersivo",
        accent_color="0x00c3e3"
    )
    vf3 = f"fps=60,scale=1920:1080,{lt3}"
    run_cmd([
        FFMPEG, "-ss", "4.5", "-i", os.path.join(MKT_DIR, "scene5_themes.mp4"),
        "-t", "10.0", "-vf", vf3, "-c:v", "libx264", "-preset", "fast",
        "-crf", "18", "-pix_fmt", "yuv420p", c3_path, "-y"
    ])

    # 6. Clip 4: Customização SteamGridDB (9.0s)
    # 31.5s - 40.5s
    print("6/7: Renderizando Clip 4 (Customização SteamGridDB)...")
    c4_path = os.path.join(TMP_DIR, "clip_4.mp4")
    lt4 = make_nintendo_card(
        tag="●  STEAMGRIDDB INTEGRADO",
        line1="Galeria de Logos 3D & Capas Oficiais",
        line2="Escolha capas, logos transparentes e a intro de cada jogo",
        accent_color="0xe60012"
    )
    vf4 = f"fps=60,scale=1920:1080,{lt4}"
    run_cmd([
        FFMPEG, "-ss", "3.0", "-i", os.path.join(MKT_DIR, "scene4_custom.mp4"),
        "-t", "9.0", "-vf", vf4, "-c:v", "libx264", "-preset", "fast",
        "-crf", "18", "-pix_fmt", "yuv420p", c4_path, "-y"
    ])

    # 7. Clip 5: Encerramento Nintendo Switch Style (4.5s)
    # 40.5s - 45.0s
    print("7/7: Renderizando Clip 5 (Encerramento)...")
    c5_path = os.path.join(TMP_DIR, "clip_5.mp4")
    vf_outro = (
        f"drawbox=x=0:y=0:w=8:h=1080:color=0xe60012:t=fill,"
        f"drawbox=x=1912:y=0:w=8:h=1080:color=0x00c3e3:t=fill,"
        f"drawtext=fontfile={FONT_BOLD}:text='DECK LAUNCHER':fontcolor=white:fontsize=76:x=(w-text_w)/2:y=360,"
        f"drawtext=fontfile={FONT_NORM}:text='Transforme seu Tablet ou TV Box em um Console Portátil':fontcolor=0x9ea6b0:fontsize=30:x=(w-text_w)/2:y=470,"
        f"drawbox=x=(w-640)/2:y=570:w=640:h=46:color=0x20242b@0.9:t=fill,"
        f"drawbox=x=(w-640)/2:y=570:w=640:h=46:color=0x383e4a@0.7:t=1,"
        f"drawtext=fontfile={FONT_BOLD}:text='100% Offline   •   Zero Anúncios   •   Nativo':fontcolor=0xe60012:fontsize=20:x=(w-text_w)/2:y=583,"
        f"fade=t=out:st=3.2:d=1.3"
    )
    run_cmd([
        FFMPEG, "-f", "lavfi", "-i", "color=c=0x14171c:s=1920x1080:r=60:d=4.5",
        "-vf", vf_outro, "-c:v", "libx264", "-preset", "fast", "-crf", "18",
        "-pix_fmt", "yuv420p", c5_path, "-y"
    ])

    # 8. Concatena todos os clipes de vídeo
    print("Concatenando clipes de vídeo...")
    concat_list = os.path.join(TMP_DIR, "concat.txt")
    with open(concat_list, "w") as f:
        for p in [c0_path, c1_path, c2a_path, c2b_path, c3_path, c4_path, c5_path]:
            f.write(f"file '{p}'\n")

    video_merged = os.path.join(TMP_DIR, "video_merged.mp4")
    run_cmd([
        FFMPEG, "-f", "concat", "-safe", "0", "-i", concat_list,
        "-c", "copy", video_merged, "-y"
    ])

    # 9. Mixagem com Trilha Nintendo (Wii Shop Lofi) e Intros Oficiais
    # Timings:
    # GameCube chime toca aos 14.0s (14000ms)
    # NDS chime toca aos 19.5s (19500ms)
    # Ducking da trilha durante os efeitos sonoros para clareza máxima
    print("Mixando trilha sonora Nintendo e efeitos de boot originais...")
    bgm_path = os.path.join(MKT_DIR, "bgm_nintendo.m4a")
    gc_audio = os.path.join(ASSETS_DIR, "gamecube.mp4")
    nds_audio = os.path.join(ASSETS_DIR, "nds.mp4")

    filter_complex = (
        "[0:a]volume='if(between(t,13.5,17.5),0.20,if(between(t,19.0,21.5),0.20,0.85))':eval=frame[bgm];"
        "[1:a]adelay=14000|14000,volume=1.4[gc];"
        "[2:a]adelay=19500|19500,volume=1.4[nds];"
        "[bgm][gc][nds]amix=inputs=3:duration=first:dropout_transition=0[aout]"
    )

    final_trailer = os.path.join(MKT_DIR, "deck_launcher_trailer.mp4")
    print(f"Gerando arquivo final: {final_trailer}...")
    run_cmd([
        FFMPEG,
        "-i", bgm_path,
        "-i", gc_audio,
        "-i", nds_audio,
        "-i", video_merged,
        "-filter_complex", filter_complex,
        "-map", "3:v",
        "-map", "[aout]",
        "-c:v", "copy",
        "-c:a", "aac",
        "-b:a", "256k",
        "-shortest",
        final_trailer, "-y"
    ])

    size_mb = os.path.getsize(final_trailer) / (1024 * 1024)
    print(f"=== SUCESSO! Trailer Nintendo Unslopped exportado: {final_trailer} ({size_mb:.2f} MB) ===")

if __name__ == "__main__":
    build_trailer()
