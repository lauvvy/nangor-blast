# Log Activity — Nangor Blast

Catatan progres tiap anggota untuk pengumpulan Milestone 2. Formatnya mengikuti spesifikasi (bagian Log Activity), jadi ditulis dalam bahasa Indonesia.

Aturan pengisian:

- Tambahkan satu baris di tabel milik sendiri setiap hari mengerjakan sesuatu.
- **Kegiatan** menyebut ID task dari `TODO.md` bila ada. **Hasil** menyebut apa yang sudah jalan dan bug yang masih ada.
- Jangan menyalin isi milik anggota lain.
- Dokumen ini diekspor menjadi satu PDF pada task `S-08`.

## Laura Paula Gosal / 18225060

| Waktu | Kegiatan | Hasil |
| --- | --- | --- |
| 10 November 2026 | Analisis spesifikasi dan penyusunan dokumen proyek | `PRD.md`, `ARCHITECTURE.md`, `TODO.md`, `WORKFLOW.md`, `DESIGN.md`, `CLAUDE.md`, dan `README.md` dibuat. Belum ada kode. |
| 10 November 2026 | `S-01` scaffold Gradle, Makefile, folder `src`, `config`, `doc` | `make build`, `make test`, `make run`, dan `make clean` jalan di Windows (`mingw32-make`). Build memakai `-Xlint:all -Werror`. Log masuk ke `logs/nangor-blast.log`. Belum dicoba di macOS atau Linux. |
| 10 November 2026 | `S-02` terminal spike dengan JLine 3.30.17 (`make spike`) | Spike membaca tombol tanpa Enter, menggambar peta 14 × 10 pada 20 FPS, dan mencatat jeda antar-tombol. Uji di terminal asli tiap anggota belum dilakukan. |
| 10 November 2026 | `C-01` core types dan contracts | `Position`, `Grid<T>`, `EventBus`, exception, `Entity`, `Player`, `GameWorld`, `GameSystem`, `Command`, `WorldSnapshot`, dan `TestWorlds` selesai. 35 unit test lulus. Menunggu review dari anggota lain. |
| 10 November 2026 | `C-02` game loop dan thread | Fixed timestep 20 tick/detik dengan batas 5 tick catch-up, tiga thread bernama (`input`, `game-loop`, `render`), key bindings dari `config/keybindings.properties`. 49 unit test lulus. Belum dicoba di terminal asli. |
| 10 November 2026 | `C-03` CLI renderer dan HUD | Peta tergambar dengan glyph dan warna dari `DESIGN.md`, dua karakter per tile, sesuai urutan prioritas gambar. HUD menampilkan nyawa, jenis bomb, skor, dan timer yang berubah merah di bawah 30 detik. Hanya sel yang berubah yang ditulis ulang, dan terminal yang terlalu kecil mendapat pesan untuk memperbesar. 58 unit test lulus. Belum dicoba di terminal asli. |
| 10 November 2026 | `C-04` gerak player dan collision | Player bergerak satu tile per langkah sesuai speed, terhalang block, enemy, bomb aktif, dan tepi peta. Gerakan yang ditekan sedikit lebih awal disimpan 0,15 detik. Menabrak enemy mengurangi satu nyawa. 68 unit test lulus. Enemy dan bomb masih memakai pengganti di test. |
| 11 November 2026 | `C-05` MVP-0 walking skeleton | `make run` menyambungkan semua komponen: menu, peta Red Mountain (sementara dari layout bawaan, belum dari file), player berjalan, timer berjalan, pause, dan LOSE saat waktu habis. 79 unit test lulus. Belum dicoba langsung di terminal asli. Format commit diganti ke Conventional Commits dengan ID task sebagai scope. |
| 11 November 2026 | Uji manual `C-05` di Windows Terminal | Peta, HUD, dan prompt tampil tanpa berkedip. Player bergerak dengan WASD dan panah, timer berjalan, pause dan resume berfungsi, dan keluar dari game mengembalikan terminal seperti semula. |
| 11 November 2026 | `C-08` cek WIN dan LOSE | Exit terkunci sampai 5 Normal Enemy dan boss dikalahkan. WIN hanya jika player berdiri di exit yang terbuka. LOSE saat nyawa atau waktu habis. Event `GameEnded` dikirim sekali untuk bonus skor. 84 unit test lulus. Enemy masih memakai pengganti. |
| 11 November 2026 | `C-06` nyawa, invulnerability, dan respawn | Setiap sumber damage mengurangi satu nyawa per aktivasi. Player muncul kembali di titik spawn, atau tile kosong terdekat jika titik spawn ditempati, dan kebal selama 1,5 detik dengan tampilan redup. Saat nyawa habis, game menjadi LOSE. 94 unit test lulus. |
| 11 November 2026 | `C-07` menu, pause, restart, dan game over | Menu utama (Start Game, How to Play, Exit), How to Play yang membaca tombol dari key bindings, pause menu (Resume, Restart, Main Menu), dan layar game over dengan skor akhir. Input yang tidak valid menampilkan pesan error. Restart membuat game baru dari awal. Menahan tombol pause hanya mengubah pause sekali. 106 unit test lulus. Belum dicoba di terminal asli. |
| 17 November 2026 | `S-05` class diagram bagian Laura | Membuat `docs/diagram/Class Diagram Core, Model, Engine, Input dan CLI.drawio` dengan empat halaman: model dasar dan tipe nilai, event dan exception, aplikasi dan engine, serta input dan tampilan CLI. Nama dan relasi dicocokkan dengan kode saat ini; GUI yang belum diimplementasikan tidak dimasukkan. |
| 17 November 2026 | `S-05` integrasi empat class diagram | Menggabungkan diagram core/model/engine/input/CLI, enemy/boss, map/mapio, dan Markdown Marshall ke `docs/diagram/Class Diagram Terintegrasi Nangor Blast.drawio`. Kelas bersama disatukan pada halaman keseluruhan; enam halaman detail menggunakan definisi yang sama. Kelas dan anggota yang belum ada di kode ditandai sebagai rancangan. Diagram sumber tetap disimpan. |

## Marshall Farrell Attalla K / 18225062

| Waktu | Kegiatan | Hasil |
| --- | --- | --- |
| 12 Nov 2026 | B-01 Bomb base dan Normal Bomb (`feat/B-01-normal-bomb`) | `Bomb`, `NormalBomb`, `BombFactory`, `BombSystem` selesai; 8 test lulus. Kapasitas masih kembali saat bom meledak, dipindah ke akhir Explosion di B-02. |
| 12 Nov 2026 | B-03 tiga jenis Block (`feat/B-03-blocks`) | `Block`, `IndestructibleBlock`, `DestructibleBlock`, `ReinforcedBlock` selesai; 8 test lulus. `#` di `TestWorlds` sekarang `IndestructibleBlock`. Drop Power-Up dari Destructible Block menyusul di B-07. |
| 15 Nov 2026 | S-02 terminal spike dan class diagram S-05 (`docs/marshall-spike-class-diagram`) | Spike jalan di terminal Claude desktop (`xterm-256color`): key terbaca tanpa Enter, tidak flicker, key release tidak dilaporkan, dua key ditahan hanya key terakhir yang berulang. Class diagram package bomb, explosion, block, power-up ditulis di `docs/diagram/class-diagram-marshall.md`. |
| 15 Nov 2026 | B-02 Explosion (`feat/B-02-explosion`) | `Explosion` dan `ExplosionSystem` selesai: pusat dan empat lengan, durasi 0,5 detik, berhenti di block, damage sekali per target, bomb lain ikut terpicu. Kapasitas bomb sekarang kembali saat ledakan selesai. 12 test lulus. |
| 15 Nov 2026 | Update progres dokumen (`docs/marshall-progress`) | B-02 ditandai Done setelah merge PR #15. F04 dicentang di `CLAUDE.md`. Catatan "As built" untuk B-01, B-03, dan B-02 ditambahkan di `ARCHITECTURE.md`. |

## Muhammad Ilham Malik Amrullah / 18225022

| Waktu | Kegiatan | Hasil |
| --- | --- | --- |
| 11 November 2026 | E-01 Patrol Enemy | `Enemy`, `MovementBehavior`, `Normal Enemy`, `PatrolBehavior` |
| 15 November 2026 | E-01 Patrol Enemy | `EnemySytem`, `GameFactory` |
| 17 November 2026 | `S-05` class diagram paket enemy dan boss | Membuat dua class diagram di draw.io. Paket enemy: `Enemy`, `NormalEnemy`, `MovementBehavior`, `PatrolBehavior`, `ChaseBehavior`, `EnemySystem`. Paket boss: `Boss`, `Hades`, `BossPhase`, `BossSystem`, `AttackPattern` dengan empat serangan. Kontrak dari C-01 yang dipakai ikut digambar. Nama serangan, field, dan method masih rancangan dan bisa berubah saat implementasi. |
| 17 November 2026 | `S-05` class diagram paket map dan mapio | Ilham membuat `docs/diagram/Class Diagram Map.drawio` dengan dua halaman: Map package dan Mapio package. File diagram ditambahkan ke folder diagram tanpa mengubah isinya. |

## Areli Matthew Widodo / 18225102

| Waktu | Kegiatan | Hasil |
| --- | --- | --- |
| | | |
