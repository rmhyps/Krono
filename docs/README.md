# Krono

Krono adalah rancangan aplikasi otomasi pribadi untuk Android. Pengguna menyusun aturan melalui pilihan visual, lalu HP menyesuaikan pengaturan ketika kejadian dan kondisi yang relevan terpenuhi.

Dua contoh utamanya adalah mengganti wallpaper berdasarkan area lokasi dan mengecilkan volume media ketika TWS tertentu tersambung.

| Informasi | Keterangan |
| --- | --- |
| Dokumen | `README.md`, nomor 9 dari 9 |
| Versi dokumentasi | 0.1 |
| Pembaruan | 22 Oktober 2026, Asia/Jakarta |
| Tahap proyek | Perencanaan sebelum implementasi |
| Platform usulan | Android |
| Titik mulai pengembangan | M00 pada [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) |

> Rangkaian ini berisi dokumentasi perencanaan. Source code aplikasi, APK, build gabungan, pipeline CI, dan hasil uji perangkat belum diserahkan melalui tahap ini. Seluruh milestone implementasi masih NOT_STARTED dan tes aplikasi masih NOT_RUN.

## 1. Tentang Krono

Krono membantu pengguna mengurangi perubahan pengaturan yang berulang. Pengguna memilih pemicu, menambahkan kondisi bila diperlukan, lalu menentukan aksi yang boleh dijalankan.

Alur sederhana berbentuk **Ketika → Lakukan**. Alur bercabang menggunakan **IF → ELSE IF → ELSE**, dengan urutan yang terlihat pada editor. Pengguna tidak perlu menulis kode untuk menyusun aturan.

Nama Krono, konsep aturan, serta dua contoh lokasi/TWS berasal dari kebutuhan pemilik produk. Android, Kotlin, Compose, penyimpanan lokal, rincian MVP, dan pembagian implementasi merupakan usulan v0.1 yang dijelaskan pada dokumen acuan.

### Contoh Wallpaper Lokasi

Contoh berikut menunjukkan perilaku, bukan source code yang dapat dijalankan.

```text
KETIKA status area yang dipantau berubah
JIKA berada di Area A
    LAKUKAN pasang gambar A
KALAU TIDAK, JIKA berada di Area B
    LAKUKAN pasang gambar B
```

Area A dan B menggunakan pusat serta radius. Perpindahan A ke B tetap relevan ketika keduanya berada di kawasan besar yang sama.

Contoh ini tidak menambahkan ELSE. Ketika seluruh kondisi diketahui tidak cocok, wallpaper terakhir dipertahankan. Jika kondisi sebelumnya belum dapat dipastikan, Krono menunggu data sesuai batas yang berlaku.

### Contoh Volume TWS

```text
KETIKA TWS pilihan pengguna tersambung
LAKUKAN atur volume media menjadi 20%
```

Angka 20% adalah contoh nilai pengguna. Perangkat lain tidak dianggap sebagai kecocokan untuk aturan tersebut.

Setelah aksi selesai, pengguna dapat mengubah volume manual. Krono tidak terus memaksakan nilai lama tanpa sebab baru yang sah. Dukungan perubahan volume background bergantung pada konteks Android yang benar-benar dibuktikan.

## 2. Status proyek

| Bagian | Status dokumentasi saat ini |
| --- | --- |
| Rangkaian dokumen | 9 berkas perencanaan v0.1 tersedia, termasuk README ini. |
| Ruang produk | Kebutuhan inti tercatat; rincian rancangan tetap berstatus usulan. |
| Rencana pengembangan | 12 milestone, M00 sampai M11, dan 60 task utama. |
| Pelaksanaan milestone/task | NOT_STARTED dalam rencana implementasi. |
| Pengujian aplikasi | NOT_RUN; belum ada PASS perangkat yang dihasilkan tahap ini. |
| Dukungan HP/TWS/OS | Menunggu pembuktian pada matriks yang ditetapkan. |
| Source code, CI, dan APK | Belum menjadi hasil rangkaian dokumentasi ini. |
| Tanggal rilis | Belum ditetapkan berdasarkan kapasitas serta bukti implementasi. |

Selesainya dokumen tidak mengubah status pembangunan aplikasi. Hasil pengujian warna, teks, atau potongan SQL pada tahap dokumentasi juga tidak menggantikan pengujian aplikasi native.

## 3. Peta dokumentasi

Gunakan nama berkas pada tabel berikut ketika menyiapkan dokumentasi di root repository Krono. Tautan relatif di README ini mengasumsikan kesembilan berkas berada dalam folder yang sama.

| Urutan | Dokumen | Pertanyaan yang dijawab |
| --- | --- | --- |
| 1 | [PRD.md](./PRD.md) | Krono menyelesaikan masalah apa, fitur mana yang dibutuhkan, dan apa kriteria penerimaannya? |
| 2 | [ANDROID_CAPABILITIES.md](./ANDROID_CAPABILITIES.md) | Apa yang diizinkan Android, akses apa yang diperlukan, dan kemampuan mana yang perlu dibuktikan? |
| 3 | [RULE_ENGINE.md](./RULE_ENGINE.md) | Bagaimana pemicu, cabang, UNKNOWN, konflik, deadline, dan hasil diputuskan? |
| 4 | [ARCHITECTURE.md](./ARCHITECTURE.md) | Bagaimana tanggung jawab kode, modul, store, runtime, dan adapter dibagi? |
| 5 | [DATABASE.md](./DATABASE.md) | Data apa yang disimpan dan bagaimana transaksi, journal, aset, serta history dijaga? |
| 6 | [DESIGN.md](./DESIGN.md) | Bagaimana layar, editor, status, komponen, dan aksesibilitas dirancang? |
| 7 | [TESTING.md](./TESTING.md) | Apa yang diuji, di lingkungan mana, dengan bukti dan syarat lulus seperti apa? |
| 8 | [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) | Apa urutan kerja, dependensi, output task, dan syarat selesai tiap tahap? |
| 9 | [README.md](./README.md) | Bagaimana memahami paket proyek dan mulai menggunakannya? |

### Jalur baca yang disarankan

- **Memahami produk:** README, PRD, lalu Design.
- **Menilai kelayakan Android:** PRD, Android Capabilities, lalu bagian PoC pada Implementation Plan.
- **Membangun inti dan data:** Rule Engine, Architecture, Database, serta kasus terkait pada Testing.
- **Membangun UI:** PRD, Design, kontrak command pada Architecture/Rule Engine, lalu tes UI.
- **Mulai mengerjakan proyek:** pahami batas produk, lalu jalankan M00 sesuai Implementation Plan.

Dokumen pemilik kontrak menjadi acuan rinci. README merangkum dan mengarahkan pembaca; perubahan ringkasan tidak otomatis mengganti perilaku yang ditetapkan dokumen lain.

## 4. Ruang MVP usulan

### Kebutuhan inti

- Membuat, membaca, mengubah, mengaktifkan, menjeda, dan menghapus aturan.
- Menyimpan draf yang belum lengkap tanpa menjalankan efek.
- Memilih satu keluarga pemicu per aturan: perubahan area atau koneksi Bluetooth.
- Menggunakan alur sederhana atau percabangan satu tingkat dengan ELSE opsional.
- Menjalankan wallpaper dan volume media, termasuk beberapa aksi berurutan.
- Menampilkan kesiapan, izin/sumber yang hilang, pekerjaan berjalan, dan hasil.
- Menyediakan simulasi logika serta uji aksi nyata secara terpisah.
- Mencegah efek ganda, menangani prioritas, serta menghormati perubahan manual.
- Menyimpan history dan memulihkan pemantauan sesuai batas Android.

### Kebutuhan tambahan yang dipilih per kandidat

Contoh aturan, duplikasi, pencarian/filter, pilihan retensi, serta durasi menetap di area berada pada kelompok Should. Fitur yang disertakan tetap harus memenuhi penerimaannya.

Kontrol dasar pertumbuhan history dan pemisahan history dari deduplikasi disiapkan pada fondasi data. Pilihan UI tambahan mengikuti cakupan kandidat yang ditetapkan.

### Kandidat lanjutan

Wi-Fi, waktu, baterai, DND, Battery Saver, AND/OR, percabangan bertingkat, cloud, akun, marketplace, root, Shizuku, serta iOS belum masuk implementasi MVP usulan.

LE Audio memerlukan pembuktian tersendiri. Dukungan Classic tidak otomatis mencakup profil, transport, atau konfigurasi earbud lain.

## 5. Perilaku yang harus tetap konsisten

| Aturan perilaku | Maknanya |
| --- | --- |
| UNKNOWN berbeda dari FALSE | Data hilang atau belum andal tidak membuat ELSE/cabang berikutnya dipilih secara keliru. |
| Cabang pertama yang cocok | Hanya satu cabang dipilih menurut urutan; gagal aksi tidak berpindah ke ELSE. |
| Aksi berurutan | Urutan mengikuti editor; langkah berikutnya berhenti ketika hasil tidak memenuhi syarat lanjut. |
| Prioritas per batch | Konflik dinilai pada satu kelompok evaluasi yang jelas; pihak kalah tidak otomatis dipromosikan jika pemenang gagal. |
| Deduplikasi berlapis | Command, event, episode, kesempatan, serta effect key tidak boleh menciptakan efek tambahan dari pengiriman ulang yang sama. |
| Pemeriksaan sebelum efek | Hak menjalankan aksi diperiksa kembali pada setiap langkah. |
| Hasil parsial tetap tercatat | Efek yang sudah terjadi tidak dianggap dibatalkan ketika langkah berikutnya gagal. |
| Pemulihan konservatif | Efek yang mungkin sudah diminta tidak diulang secara buta setelah proses mati. |
| Pengguna tetap mengendalikan HP | Jeda menghentikan pekerjaan baru yang tidak lagi sah; perubahan manual tidak menjadi loop pemaksaan. |
| Membaca layar tidak memicu aksi | Refresh, rotasi, dan koleksi ulang state tidak membuat command aktivasi/uji baru. |

Aktivasi atau penyimpanan revisi aktif dapat memeriksa keadaan terkini satu kali sesuai kontrak. UI perlu menjelaskan bahwa aksi yang cocok bisa langsung berjalan setelah pengguna menyimpan/mengaktifkan.

### Empat jenis tindakan pemeriksaan

| Tindakan | Perilaku |
| --- | --- |
| Simulasi | Memakai input contoh untuk memeriksa logika; tidak mengubah HP atau kontrol eksekusi produksi. |
| Periksa kesiapan | Probe akses/sumber yang dibatasi; dapat melanjutkan kesempatan lama yang masih sah tanpa membuat retry baru. |
| Coba aturan sekarang | Command pengguna baru untuk memeriksa kondisi terkini; aksi dapat berjalan jika sah. |
| Uji aksi | Mencoba aksi terpilih secara nyata dengan guard kemampuan dan penjelasan dampaknya. |

Simulasi satu aturan tidak menyatakan konflik semua aturan sudah diperiksa. Uji aksi saat layar terlihat tidak membuktikan pemicu background.

## 6. Arah tampilan

Usulan desain menggunakan Jetpack Compose dan Material 3, dengan tema gelap, aksen ungu, serta editor vertikal. Dua tujuan navigasi utama adalah **Aturan** dan **Riwayat**, dengan akses terpisah ke **Pengaturan**.

UI membedakan empat dimensi: keinginan aktif, kesiapan, pekerjaan berjalan, dan hasil terakhir. Aturan dapat tetap diinginkan aktif ketika aksesnya hilang; hambatannya ditampilkan pada kesiapan.

Editor menyediakan pemicu, cabang, aksi, validasi, dan tinjau. Token warna/ukuran, 12 konteks layar, copy, layout adaptif, serta aksesibilitas dirinci pada [DESIGN.md](./DESIGN.md). Referensi visual awal memberi arah tampilan, sementara daftar fitur mengikuti kebutuhan produk.

## 7. Arsitektur dan teknologi

### Lima modul awal

| Modul | Tanggung jawab |
| --- | --- |
| `:app` | Compose, ViewModel, navigasi, composition root, dan konfigurasi aplikasi. |
| `:core:engine` | Model, validasi, evaluator, episode, konflik, hasil, dan kontrak port dalam Kotlin/JVM. |
| `:core:runtime` | Command, coordinator, rencana, guard, journal, dan recovery dalam Kotlin/JVM. |
| `:data:local` | Room, DataStore, file privat, transaksi, migrasi, serta proyeksi history. |
| `:platform:android` | Lokasi, Bluetooth, capability, efek, receiver, worker, serta sesi yang terbukti layak. |

Dua modul core tidak mengimpor Android, Compose, Room, Hilt, atau WorkManager. UI dan entry point Android menyerahkan command/masukan ke runtime yang sama. Setter tidak dipanggil langsung dari slider, ViewModel, receiver, atau worker melalui jalur sendiri.

Database mencatat fase permintaan sebelum API efek dipanggil. Database dan perubahan OS tetap bukan satu transaksi atomik; hasil yang tidak pasti perlu dilaporkan dengan jujur.

### Baseline usulan

| Bagian | Pilihan |
| --- | --- |
| Android | Minimum API 29; compile/target API 37 sebagai usulan awal. |
| Bahasa dan state | Kotlin, Coroutines, Flow, ViewModel, StateFlow. |
| UI dan navigasi | Compose, Material 3, Navigation 3. |
| Data | Room 3, namespace `androidx.room3`; preferensi tampilan melalui DataStore. |
| Driver dan journal | AndroidSQLiteDriver dengan TRUNCATE, mengikuti Database. |
| Penyusunan dependensi | Hilt. |
| Pengiriman kerja tertunda | WorkManager. |
| Lokasi dan efek | GeofencingClient, API profil Bluetooth, WallpaperManager, AudioManager. |
| Toolchain acuan | AGP 9.4.0, Gradle 9.6.0, JDK 17. [R01] |

Seluruh rincian versi berada di [ARCHITECTURE.md](./ARCHITECTURE.md). Versi minor library tidak disalin semua ke README agar penguncian tetap mempunyai satu acuan.

Room 3 mempunyai namespace/API yang perlu diikuti implementasi store Krono. Halaman rilis resminya menjadi sumber pengecekan versi serta schema export. [R03]

Kombinasi compiler, KSP, Hilt, serta runner belum dianggap terbukti kompatibel sebelum bootstrap. Versi yang benar-benar berhasil dibangun dikunci pada M00.

## 8. Batas Android yang memengaruhi produk

### Audio background

Android 17 membatasi interaksi audio background. Pada target API 37, konteks background membutuhkan foreground service dengan kemampuan while-in-use yang sesuai. Setter volume dapat gagal diam-diam jika lifecycle tidak sah. [R02]

Karena itu, sesi audio menjadi PoC awal. Menerima event TWS dan mempunyai hak mengubah volume adalah dua kemampuan yang perlu dibuktikan. Nama layanan atau tidak adanya exception belum cukup menjadi bukti.

### Lokasi

Area memakai pusat/radius, kualitas data, serta status registrasi yang nyata. Pengiriman geofence dapat terlambat; detail batas dan eksperimennya berada pada Android Capabilities.

Perubahan A/B, overlap, akses yang dicabut, dan data UNKNOWN diperiksa tersendiri. Rancangan lokal tidak menjamin seluruh sumber geofence bekerja sepenuhnya offline atau pada perangkat tanpa layanan yang diperlukan.

### Perangkat dan wallpaper

Koneksi Bluetooth dan rute audio aktif diperiksa terpisah. Pengaturan volume media tidak dianggap khusus tersimpan per TWS.

Sasaran wallpaper mengikuti dukungan perangkat/policy. Pilihan gambar diproses melalui impor privat; URI dari picker belum langsung berarti gambar siap.

### Gangguan dan pemulihan

Kematian proses, membuang task, Force Stop, reboot, dan update adalah keadaan berbeda. Pemantauan direkonsiliasi sesuai kemampuan; sesi audio lama tidak dianggap otomatis hidup kembali.

Tidak ada klaim dukungan seluruh HP/OS dari rancangan atau satu percobaan. Daftar dukungan mengikuti hasil pada [TESTING.md](./TESTING.md).

## 9. Langkah pertama pengembangan

Mulai dari **M00**, dengan daftar task serta syarat selesai pada [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md).

1. Gunakan kesembilan dokumen dengan nama berkas yang konsisten.
2. Pahami PRD serta batas Android untuk dua contoh inti.
3. Catat lingkungan pengembangan, toolchain, dan perangkat yang benar-benar tersedia.
4. Buat lima modul, kunci versi awal, dan buktikan build/runner.
5. Siapkan graph tunggal, fixture, serta pencatatan bukti.
6. Uji efek foreground lalu jalur audio Android 17 sejak awal.
7. Lanjutkan engine/store ketika eksperimen perangkat belum selesai.
8. Sambungkan runtime serta journal sebelum aturan produksi boleh menjalankan setter.

### Task awal

| Task | Hasil yang dituju |
| --- | --- |
| IMP-001 | Baseline versi dan inventaris lingkungan. |
| IMP-002 | Lima modul dengan dependensi yang benar. |
| IMP-003 | Build, runner, serta CI minimum. |
| IMP-004 | Composition root dan entry point yang aman. |
| IMP-005 | Backlog serta fixture awal. |
| IMP-006, IMP-007 | Efek foreground dan pembuktian konteks audio Android 17. |

### Menjalankan aplikasi

Pada tahap ini belum ada prosedur clone, build, install, atau unduh APK Krono yang terverifikasi. Perintah operasional ditambahkan setelah source code, Wrapper, varian, dan artefak nyata tersedia.

Contoh perintah pada Testing adalah pola untuk proyek yang nanti dibuat. Ia belum merupakan laporan task Gradle yang sudah ada atau berhasil dijalankan.

## 10. Roadmap ringkas

Semua milestone berikut berstatus **NOT_STARTED** dalam rencana implementasi.

| Milestone | Hasil |
| --- | --- |
| M00 | Fondasi proyek, build, runner, dan inventaris bukti. |
| M01 | PoC efek, audio Android 17, TWS, serta geofence. |
| M02 | Model, evaluator, episode, arbiter, dan port core. |
| M03 | Store Room, constraint, transaksi, dan aset dasar. |
| M04 | Runtime, guard, journal, outbox, serta recovery. |
| M05 | Adapter efek/aset dan capability produksi. |
| M06 | Pemantauan sumber serta entry point Android. |
| M07 | Alur Volume TWS terintegrasi. |
| M08 | Alur Wallpaper Lokasi A/B terintegrasi. |
| M09 | UI lengkap, Settings, serta aksesibilitas. |
| M10 | Regresi, matriks dukungan, performa, dan penyelesaian bug. |
| M11 | Kandidat rilis internal dan serah terima bukti. |

Laporan PoC dapat selesai dengan hasil gagal. Gate kemampuan fitur yang bergantung pada keberhasilan tersebut tetap terbuka.

M07 dan M08 memakai fondasi sama. Pekerjaan wallpaper dapat dilanjutkan ketika jalur TWS tertentu terhalang, dengan status kebutuhan TWS yang tetap jelas.

Detail 60 task, dependensi, risiko, dan Definition of Done berada pada Implementation Plan. Belum ada tanggal penyelesaian yang ditetapkan dari kapasitas kerja nyata.

## 11. Pengujian dan penerimaan

[TESTING.md](./TESTING.md) menetapkan **27 rute tes** serta memetakan **160 kasus penerimaan/eksperimen sumber**. Angka tersebut menunjukkan cakupan rencana, bukan jumlah tes otomatis yang sudah ditulis atau lulus.

Pengujian dibagi menjadi unit/core JVM, integrasi data, adapter Android, component/screen Compose, alur aplikasi, serta perangkat/lapangan. Fake dipakai untuk logika; radio, geofence, audio background, dan variasi vendor memerlukan bukti yang sesuai.

| Gate | Fokus |
| --- | --- |
| G-01 | Build dan graph yang dapat ditelusuri. |
| G-02 | Logika serta kontrol efek. |
| G-03 | Integritas data, aset, dan recovery. |
| G-04 | Kemampuan platform yang benar-benar dibuktikan. |
| G-05 | Alur pengguna serta aksesibilitas. |
| G-06 | Kualitas, performa, dan bug penghalang. |
| G-07 | Hasil pelaporan yang cocok dengan kandidat build. |

Status tes aplikasi saat dokumentasi ini disusun adalah **NOT_RUN**. NOT_RUN/BLOCKED tidak dihitung sebagai PASS.

Tes yang sengaja membuat aksi gagal dapat PASS ketika Krono menanganinya dengan benar. Hal tersebut membuktikan penanganan hambatan, sementara kemampuan aksi yang terhalang tetap memerlukan pembuktian sukses tersendiri.

## 12. Keputusan yang masih perlu ditutup

| Keputusan | Tempat penyelesaian |
| --- | --- |
| Kombinasi compiler, KSP, Hilt, runner, dan plugin yang kompatibel. | M00 dan baseline Architecture. |
| Sesi audio Android 17, jenis layanan, start/stop, serta recovery. | M01/M05 dan Android Capabilities. |
| Perangkat, profil Classic/LE, rute audio, serta OS yang didukung. | PoC dan matriks M10. |
| Provider peta serta cakupan perangkat tanpa Play services. | Android Capabilities, Design, M01/M08. |
| Batas gambar, performa, daya, dan evaluasi parameter usulan. | Baseline M05/M10 dan dokumen pemilik kontrak. |
| Fitur Should yang masuk kandidat pertama. | PRD dan M09. |
| Kanal distribusi, identitas kandidat, dan kebijakan lisensi proyek. | Keputusan proyek sebelum materi rilis diterbitkan. |

Keputusan teknis yang sudah dipertegas oleh dokumen lanjutan tetap berstatus usulan v0.1 sampai ditetapkan dalam pelaksanaan. Catat perubahan beserta alasan, dampak, tanggal, dan bukti yang diperlukan.

## 13. Menjaga dokumentasi tetap sesuai proyek

| Perubahan | Acuan yang diperbarui |
| --- | --- |
| Ruang fitur atau penerimaan pengguna | PRD. |
| Dukungan API/perangkat atau hasil PoC | Android Capabilities dan bukti Testing. |
| Semantik aturan, episode, konflik, atau deadline | Rule Engine dan kasus terkait. |
| Modul, dependensi, versi, atau lifecycle komponen | Architecture. |
| Schema, transaksi, aset, retensi, atau migrasi | Database. |
| Layar, microcopy, status, atau aksesibilitas | Design. |
| Skenario, lingkungan, gate, atau hasil tes | Testing. |
| Urutan task, dependensi, status, atau blocker | Implementation Plan. |
| Ringkasan status, navigasi, atau petunjuk mulai | README. |

Jika dua dokumen berbeda, telusuri kontrak pemiliknya dan catat ketidaksesuaian. Jangan memilih arti baru dari ringkasan README lalu membiarkan acuan lain tertinggal.

Setelah aplikasi tersedia, README diperbarui dengan perintah yang sudah dijalankan, identitas build, matriks dukungan, kanal rilis, serta informasi proyek yang telah ditetapkan. Tautan repository Krono, badge CI, APK, dan lisensi tidak diisi dengan contoh yang seolah sudah resmi.

## 14. Referensi resmi

Diperiksa pada **22 Oktober 2026**. Sumber menjelaskan API atau contoh implementasi, sedangkan kemampuan Krono tetap memerlukan tesnya sendiri.

| ID | Sumber | Pemakaian |
| --- | --- | --- |
| R01 | [AGP 9.4.0 release notes](https://developer.android.com/build/releases/agp-9-4-0-release-notes) | Acuan AGP, Gradle, dan JDK. |
| R02 | [Android 17 background audio hardening](https://developer.android.com/about/versions/17/changes/bg-audio) | Kelayakan audio serta silent failure. |
| R03 | [Room 3 release notes](https://developer.android.com/jetpack/androidx/releases/room3) | Namespace, versi, serta schema export. |
| R04 | [android/nowinandroid](https://github.com/android/nowinandroid) | Acuan aplikasi Kotlin/Compose dan pola pengujian. |
| R05 | [android/platform-samples](https://github.com/android/platform-samples) | Contoh API platform Android. |
| R06 | [androidx/androidx](https://github.com/androidx/androidx) | Penelusuran sumber Jetpack. |
| R07 | [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Coroutine dan test scheduler. |

Repository resmi menjadi bahan acuan. Tag/commit yang diadopsi dicatat saat implementasi, bersama versi serta lisensi yang berlaku. Daftar referensi lebih rinci tersedia pada masing-masing dokumen.

