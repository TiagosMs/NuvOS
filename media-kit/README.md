# 🎮 Deck Launcher - Media Kit Completo

Kit completo de mídia contendo capturas em alta resolução, vídeos de navegação em tempo real, todos os áudios e efeitos sonoros da interface, bem como todos os vídeos de abertura dos consoles para edição de vídeos promocionais e demonstrações.

---

## 📁 Estrutura de Diretórios

```
media-kit/
├── videos/                  # Gravações da tela do tablet (MP4)
│   └── 01_tour_interface.mp4  (Navegação pelas abas de Jogos, Emuladores, Pastas e Menus)
│
├── screenshots/             # Capturas de tela sem perdas (PNG - Resolução Nativa)
│   ├── 01_home_jogos.png                      (Tela inicial de Jogos com carrossel e capas)
│   ├── 02_aba_emuladores.png                  (Visão de consoles e emuladores integrados)
│   ├── 03_aba_apps.png                        (Aba de Aplicativos e pastas)
│   ├── 04_dentro_pasta_apps.png               (Dentro da pasta de apps com carrossel)
│   ├── 05_drawer_opcoes_jogo.png              (Drawer lateral de opções rápidas)
│   ├── 06_drawer_personalizar_metadados.png   (Drawer lateral de metadados: capa, banner, logo, som)
│   ├── 07_start_menu_biblioteca.png           (Menu inicial - Aba Biblioteca)
│   ├── 08_start_menu_aparencia.png            (Menu inicial - Aba Aparência)
│   └── 09_start_menu_bancos_dados.png         (Menu inicial - Aba Bancos de Dados)
│
├── audio/
│   ├── effects/             # Efeitos sonoros originais da interface (WAV sem compressão)
│   │   ├── sfx_nav_move.wav        (Movimento direcional D-Pad/Analógico)
│   │   ├── sfx_tab_switch.wav      (Alternância entre abas L1/R1)
│   │   ├── sfx_confirm.wav         (Confirmação / Botão A)
│   │   ├── sfx_cancel_back.wav     (Voltar / Botão B)
│   │   ├── sfx_menu_open.wav       (Abertura de menu/drawer lateral)
│   │   ├── sfx_menu_close.wav      (Fechamento de menu/drawer lateral)
│   │   ├── sfx_game_launch.wav     (Efeito sonoro ao inicializar jogo/emulador)
│   │   ├── sfx_nav_boundary.wav    (Toque no limite da lista)
│   │   ├── sfx_volume_up.wav       (Aumento de volume)
│   │   ├── sfx_volume_down.wav     (Diminuição de volume)
│   │   └── sfx_sleep.wav           (Suspensão de tela)
│   │
│   └── intros/              # Vídeos e áudios de abertura dos consoles (MP4 Full Screen)
│       ├── ps1.mp4                 (PlayStation 1)
│       ├── ps2.mp4                 (PlayStation 2)
│       ├── psp.mp4                 (PlayStation Portable)
│       ├── xbox_classic.mp4        (Xbox Clássico)
│       ├── xbox_360.mp4            (Xbox 360)
│       ├── xbox_series.mp4         (Xbox Series)
│       ├── switch.mp4              (Nintendo Switch)
│       ├── wii.mp4                 (Nintendo Wii)
│       ├── wiiu.mp4                (Nintendo Wii U)
│       ├── gamecube.mp4            (Nintendo GameCube)
│       ├── 3ds.mp4                 (Nintendo 3DS)
│       ├── nds.mp4                 (Nintendo DS)
│       └── dreamcast.mp4           (Sega Dreamcast)
│
└── capture_all.sh           # Script automatizado para regravação de mídia via ADB
```

---

## 🎬 Dicas para Edição de Vídeo

1. **Tour de Interface (`01_tour_interface.mp4`)**:
   - Resolução nativa capturada a 12 Mbps.
   - Mostra as transições suaves de abas, efeito de blur gaussiano no fundo e os novos drawers deslizantes.
2. **Sincronização de Áudio**:
   - Utilize os arquivos em `audio/effects/` sobrepostos nos cortes e cliques de botões para efeito de áudio estéreo imersivo.
3. **Vídeos de Abertura (`audio/intros/`)**:
   - Podem ser usados diretamente na timeline como transição de abertura ao apresentar cada emulador.
