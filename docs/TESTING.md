# Krono

## Testing Strategy and Acceptance Plan

| Atribut | Keterangan |
| --- | --- |
| Nama produk | Krono |
| Nama dokumen | `TESTING.md` |
| Nomor dalam rangkaian | 7 dari 9 |
| Versi dokumen | 0.1 |
| Tanggal penyusunan | 22 Oktober 2026, Asia/Jakarta |
| Bahasa | Bahasa Indonesia |
| Tahap | Rencana pengujian sebelum implementasi |
| Status keputusan | Usulan v0.1, mengikuti kontrak enam dokumen sebelumnya |
| Status pengujian aplikasi | **NOT_RUN: belum dijalankan** |
| Platform usulan | Android, Kotlin, Jetpack Compose, penyimpanan lokal |

> Dokumen ini menetapkan cara membuktikan perilaku Krono. Dokumen ini bukan laporan hasil pengujian. Aplikasi native, database Room lengkap, otomasi perangkat, dan pipeline CI belum dibuat atau dijalankan pada tahap penyusunan dokumen ini. Pemeriksaan teks, warna, atau potongan SQL pada tahap dokumentasi tidak membuktikan kemampuan aplikasi.

## 1. Tujuan dan cara memakai dokumen

Pengujian Krono harus menjawab dua pertanyaan: apakah aturan diputuskan dengan benar, dan apakah Android benar-benar mengizinkan aksi yang diminta dalam keadaan perangkat tersebut.

Keduanya dibuktikan secara terpisah. Evaluator yang lolos di JVM belum membuktikan receiver menerima koneksi TWS. Setter yang berhasil ketika layar Krono terbuka belum membuktikan setter bekerja setelah layar terkunci.

Dokumen ini menjadi acuan untuk:

1. Menulis tes ketika modul mulai diimplementasikan.
2. Menyiapkan perangkat, data contoh, dan kondisi pengujian.
3. Mencatat hasil serta bukti tanpa mengubah kegagalan menjadi klaim berhasil.
4. Menentukan apakah fitur layak masuk versi uji atau rilis.
5. Menghubungkan hasil tes dengan kebutuhan produk dan kontrak teknis.

### 1.1 Hubungan dengan dokumen sebelumnya

| Dokumen | Kontrak yang diperiksa |
| --- | --- |
| `PRD.md` | FR-01 sampai FR-26 dan AC-01 sampai AC-13. |
| `ANDROID_CAPABILITIES.md` | Kelayakan API, akses, lifecycle, serta eksperimen V-01 sampai V-25. |
| `RULE_ENGINE.md` | Cabang, UNKNOWN, episode, konflik, deadline, hasil, serta RE-T01 sampai RE-T40. |
| `ARCHITECTURE.md` | Batas modul, satu runtime, pemulihan, transaksi, serta ARCH-T01 sampai ARCH-T26. |
| `DATABASE.md` | Constraint, jurnal, aset, outbox, riwayat, serta DB-T01 sampai DB-T28. |
| `DESIGN.md` | Status, alur layar, command, aksesibilitas, serta DES-T01 sampai DES-T28. |

Rincian perilaku tetap berasal dari dokumen pemilik kontrak. TESTING.md menjelaskan cara memeriksanya. Jika hasil pengujian menuntut perubahan perilaku, perbarui dokumen pemilik kontrak dan kasus terkait sebelum mengubah hasil yang diharapkan.

### 1.2 Istilah hasil tes

| Status tes | Makna |
| --- | --- |
| NOT_RUN | Tes belum dijalankan terhadap build dan lingkungan yang dicatat. |
| PASS | Hasil aktual memenuhi seluruh pemeriksaan kasus pada lingkungan yang dicatat, dengan bukti yang cukup. |
| FAIL | Ada hasil aktual yang bertentangan dengan hasil yang diharapkan. |
| BLOCKED | Tes tidak dapat diselesaikan, misalnya perangkat/API atau konfigurasi yang dibutuhkan belum tersedia. |
| NOT_APPLICABLE | Kasus tidak berlaku pada ruang dukungan yang sudah ditetapkan, dengan alasan tertulis. |

`BLOCKED` bukan `PASS`. `NOT_APPLICABLE` tidak dipakai untuk menyembunyikan kebutuhan Must yang belum terpenuhi. Satu kasus dapat PASS pada satu perangkat dan FAIL pada perangkat lain.

Status tes juga berbeda dari hasil eksekusi aplikasi. Contohnya, tes yang sengaja membuat setter gagal dapat PASS ketika Krono mencatat `FAILED` dengan benar. Namun, PASS tersebut tidak membuktikan fitur volume background sudah berfungsi.

### 1.3 Istilah teknis singkat

| Istilah | Arti dalam dokumen |
| --- | --- |
| Fixture | Data contoh dengan keadaan yang sengaja dibuat tetap agar hasil bisa dibandingkan. |
| Fake / test double | Pengganti komponen asli di tes, misalnya sumber lokasi buatan. |
| Assertion | Pemeriksaan yang menentukan hasil aktual sesuai harapan atau tidak. |
| Barrier | Titik tahan terkontrol agar urutan dua pekerjaan dapat diatur untuk menguji race. |
| Race | Hasil yang dipengaruhi pekerjaan yang saling mendahului atau berjalan bersamaan. |
| CAS | Pembaruan hanya berhasil jika versi/state lama masih sesuai; mencegah klaim ganda. |
| Epoch | Identitas satu masa hidup runtime; pekerjaan lama tidak mendapat hak dari runtime baru. |
| Idempotent | Pengiriman ulang permintaan sama tidak menambah efek bisnis baru. |
| Instrumentasi | Tes yang dijalankan dalam lingkungan Android pada emulator/perangkat. |
| FGS / WIU | Foreground service dan kemampuan while-in-use yang relevan bagi kelayakan aksi Android. |
| PoC | Percobaan kecil untuk membuktikan jalur teknis sebelum fitur dijanjikan. |
| Regresi | Perilaku yang sebelumnya benar lalu rusak setelah perubahan. |

## 2. Ruang lingkup dan batas pembuktian

### 2.1 Yang diuji untuk MVP usulan

- Membuat, menyimpan, mengubah, mengaktifkan, menjeda, dan menghapus aturan.
- Satu keluarga pemicu per aturan: perubahan area atau koneksi Bluetooth pilihan.
- Alur sederhana serta IF/ELSE IF/ELSE satu tingkat.
- Wallpaper dan volume media, termasuk beberapa aksi berurutan.
- Draft, simulasi, uji aksi nyata, kesiapan, dan riwayat.
- Deduplikasi, prioritas, perubahan manual, deadline, serta pemulihan.
- Transaksi, constraint, migrasi, aset gambar, retensi, dan penghapusan riwayat.
- Tampilan Compose, aksesibilitas, perubahan jendela, dan lifecycle.
- Kemampuan Android pada kombinasi OS, target SDK, perangkat, dan akses yang dicatat.

### 2.2 Yang tidak dianggap tersedia

Pemicu Wi-Fi, jadwal, baterai, DND, Battery Saver, percabangan bertingkat, AND/OR, akun, sinkronisasi cloud, root, dan Shizuku belum masuk MVP usulan.

Tes tetap memeriksa bahwa konfigurasi di luar cakupan ditolak atau ditampilkan sebagai belum tersedia. Tidak perlu membangun fitur tersebut untuk meluluskan tes.

LE Audio mempunyai eksperimen tersendiri. Dukungan TWS Classic tidak otomatis mencakup LE Audio, kedua earbud, atau perpindahan transport.

### 2.3 Pernyataan dukungan harus spesifik

Gunakan bentuk berikut ketika hasil sudah tersedia:

“Volume TWS Classic diuji pada perangkat X, Android Y, target SDK Z, profil A2DP, dengan sesi yang dimulai melalui alur U.”

Hindari kesimpulan seperti “semua Android didukung” dari satu perangkat. Catat dukungan lokasi, penerimaan Bluetooth, wallpaper, dan volume secara terpisah.

Jika otomasi volume background Android 17 belum terbukti, kebutuhan inti pada kombinasi tersebut masih terhalang. Teks bantuan yang jujur diperlukan, tetapi tidak menggantikan kemampuan yang dijanjikan produk.

## 3. Prinsip pengujian Krono

### 3.1 Invarian yang menjadi pemeriksaan utama

| Prinsip | Bukti yang dicari |
| --- | --- |
| Keputusan deterministik | Snapshot, konfigurasi, urutan, dan waktu yang sama menghasilkan keputusan yang sama. |
| UNKNOWN berbeda dari FALSE | Data yang hilang tidak membuat ELSE atau cabang berikutnya berjalan secara keliru. |
| Satu cabang | Hanya cabang pertama yang memenuhi kontrak dipilih. Gagalnya aksi tidak memilih cabang lain. |
| Kejadian ganda tidak menggandakan efek | Command, event, episode, kesempatan, rencana, dan effect key diperiksa sesuai lapisannya. |
| Prioritas berlaku per batch | Seluruh cabang dipertimbangkan; pemenang gagal tidak menghidupkan pihak yang kalah. |
| Guard diperiksa sebelum setiap aksi | Revisi, generasi, epoch, deadline, data, kemampuan, dan seluruh target tetap sah. |
| Jurnal mendahului setter | Kegagalan menyimpan REQUESTING menghasilkan nol panggilan setter baru. |
| Hasil tidak pasti tetap tidak pasti | Proses mati setelah permintaan tidak menyebabkan pengulangan efek buta atau klaim sukses palsu. |
| Satu invoker efek | Krono tidak memanggil dua setter secara paralel melalui runtime sendiri. |
| Pengguna tetap mengendalikan HP | Perubahan manual dan pembukaan layar tidak menjadi alasan memaksakan nilai lama. |
| Simulasi tanpa efek | Tidak ada setter, registrasi sumber, kesempatan produksi, atau riwayat eksekusi produksi. |
| Riwayat bukan kontrol deduplikasi | Menghapus tampilan riwayat tidak menghapus penahan efek lama. |

Terapkan prinsip ini pada hasil publik dan catatan panggilan port. Jangan hanya memeriksa bahwa fungsi internal tertentu dipanggil.

### 3.2 Menguji kontrak, bukan menyalin implementasi

Kasus yang baik menentukan input dan hasil berdasarkan kebutuhan, lalu tetap berguna ketika struktur kode berubah.

Contoh: “A UNKNOWN, B TRUE menghasilkan menunggu data dan nol setter” lebih kuat daripada tes yang hanya memastikan sebuah fungsi helper dipanggil sekali.

Untuk aturan waktu dan transisi, tulis contoh batas serta urutan kejadian yang dapat membantah implementasi. Assertion yang dihitung menggunakan fungsi produksi yang sama dengan kode yang diuji bukan pembanding independen.

### 3.3 Ukuran kelulusan

Tidak ada target persentase coverage yang dianggap cukup untuk rilis. Coverage dapat menunjukkan bagian yang belum tersentuh, tetapi tidak menggantikan pemeriksaan kejadian ganda, race, dan perangkat nyata. [S01][S02]

Usulan ukuran utama adalah seluruh kasus wajib yang berlaku lulus, tidak ada bug penghalang terbuka, dan bukti kemampuan platform sesuai klaim dukungan. Angka coverage dicatat sebagai informasi pendukung.

## 4. Risiko, prioritas, dan tingkat keparahan

Prioritas menentukan urutan tes. Tingkat keparahan menentukan dampak bug yang ditemukan. Keduanya tidak selalu sama.

### 4.1 Prioritas tes

| Prioritas | Fokus | Contoh |
| --- | --- | --- |
| P0 | Efek yang salah, berulang, terlambat tanpa hak, kehilangan kontrol/data, atau sukses palsu. | ELSE saat UNKNOWN; setter setelah jeda; ulang efek setelah proses mati. |
| P1 | Alur inti, persistensi, kemampuan perangkat, keterbacaan, dan penjelasan hambatan. | Pilih TWS yang benar; impor gambar; form pada font 200%. |
| P2 | Penyempurnaan atau fitur Should yang sudah masuk build. | Filter tambahan, transisi visual, pencarian pada daftar panjang. |

Seluruh P0 dan P1 yang berlaku pada ruang dukungan rilis wajib lulus. Fitur Should yang belum dipilih tidak otomatis menjadi penghalang MVP; kontrak kontrol yang dipakai fitur inti tetap wajib.

### 4.2 Tingkat keparahan bug

| Tingkat | Dampak | Perlakuan usulan |
| --- | --- | --- |
| Critical | Efek perangkat salah/berulang tanpa permintaan sah, kehilangan konfigurasi, atau kontrol pemulihan rusak. | Menghalangi rilis sampai diperbaiki dan diuji ulang. |
| High | Alur inti gagal, hasil keberhasilan menyesatkan, atau aksesibilitas menghalangi alur utama. | Menghalangi rilis pada ruang dukungan terdampak. |
| Medium | Alur tambahan terganggu tetapi tersedia jalan lain yang jelas. | Dinilai berdasarkan fitur yang disertakan dan dampaknya. |
| Low | Kekurangan tampilan tanpa kehilangan makna atau interaksi utama. | Dapat dijadwalkan dengan catatan. |

Race yang baru ditemukan satu kali tetap dapat Critical. Frekuensi rendah tidak menghapus dampak efek yang tidak diminta.

## 5. Lapisan pengujian

Strategi mengutamakan tes kecil untuk logika, lalu menambah tes integrasi dan perangkat pada batas yang tidak dapat dibuktikan oleh fake. Pembagian ini mengikuti panduan Android tentang ukuran dan kemiripan lingkungan tes terhadap produksi. [S01]

| Kode | Lapisan | Tempat utama | Yang dibuktikan | Batasnya |
| --- | --- | --- | --- | --- |
| U | Unit/komponen JVM | `:core:engine`, `:core:runtime` | Evaluasi, state machine, guard, urutan, waktu, dan kontrak port. | Tidak membuktikan Android atau SQLite perangkat. |
| D | Integrasi data | `:data:local`, instrumentasi | Room, constraint, transaksi, CAS, query, file, dan migrasi. | Tidak membuktikan izin atau penerimaan event radio. |
| A | Integrasi adapter Android | `:platform:android` | Pemetaan API, receiver, worker, capability, dan hasil port. | Sumber palsu tidak membuktikan event sistem asli. |
| C | Component/screen Compose | `:app` | Semantics, state, navigasi, input, command, serta pemulihan UI. | Gambar layar dan fake tidak membuktikan efek perangkat. |
| E | End-to-end aplikasi | Emulator/perangkat | Alur UI sampai penyimpanan, runtime, dan hasil yang terintegrasi. | Lingkungan tes dapat berbeda dari lifecycle penggunaan sehari-hari. |
| H | Perangkat dan lapangan | Perangkat fisik/TWS | Radio, geofence nyata, audio background, wallpaper, vendor, Doze. | Berlaku pada kombinasi yang benar-benar diuji. |

Satu kasus dapat memerlukan beberapa lapisan. RE-T25, misalnya, memerlukan fake setter yang diam di unit test dan pemeriksaan audio Android 17 pada perangkat.

### 5.1 Penempatan dan arah dependensi

Tes mengikuti lima modul pada ARCHITECTURE.md. Dua modul core tetap dapat dibangun dan diuji tanpa Android SDK di classpath produksi core.

`:app` boleh merangkai fake melalui dependency injection pada build tes. Core tidak mendapat dependensi Compose, Room, Hilt, Context, atau API Android hanya agar tes lebih mudah.

Tambahkan pemeriksaan dependensi saat bootstrap proyek. Periksa juga aplikasi, receiver, worker, dan layanan memakai instance runtime/invoker yang sama dalam satu proses.

### 5.2 Pemilihan test double

| Double | Penggunaan | Pemeriksaan tambahan |
| --- | --- | --- |
| Fake clock | Waktu monoton dan kalender dikendalikan terpisah. | Epoch serta batas deadline diuji eksplisit. |
| Fake source | Fakta TRUE/FALSE/UNKNOWN, kualitas, provenance, dan episode. | Tidak menganggap fake sebagai bukti geofence/Bluetooth. |
| Fake capability | Layak, terhalang, pulih, atau belum diketahui. | Tes perangkat memeriksa kemampuan sebenarnya. |
| Recording effect port | Mencatat sasaran, parameter, urutan, dan jumlah permintaan. | Bedakan pembacaan status dari setter. |
| Controllable effect port | Menahan permintaan di barrier lalu mengembalikan hasil yang dipilih. | Ingress tetap diproses ketika invoker sibuk. |
| Fake dispatcher/outbox | Mengirim ulang atau menunda referensi pekerjaan. | Integrasi WorkManager diuji terpisah. |
| Database Room nyata | Constraint dan transaksi pada driver produksi. | In-memory saja belum membuktikan reopen/crash/migrasi. |

Utamakan fake kecil dengan perilaku jelas. Jangan memakai database palsu untuk mengklaim unique constraint SQLite sudah terbukti. [S03]

## 6. Lingkungan dan alat pengujian

### 6.1 Baseline yang mengikuti Architecture

| Komponen | Baseline usulan | Perlakuan dalam pengujian |
| --- | --- | --- |
| Android minimum | API 29 | Uji instalasi dan alur inti; keputusan produk masih usulan. |
| Compile/target SDK | 37 | Skenario Android 17 diuji pada target rilis ini. |
| Toolchain | AGP 9.4.0, Gradle 9.6.0, JDK 17 | Mengikuti ARCHITECTURE.md; kombinasi lengkap harus berhasil dibangun dahulu. |
| Compose | BOM 2026.09.00 | Artefak tes mengikuti versi yang kompatibel dalam katalog proyek. |
| Database | Room 3.0.3, namespace `androidx.room3` | Tes memakai namespace serta API Room 3, bukan menyalin setup Room 2. |
| SQLite | AndroidSQLiteDriver, TRUNCATE | Verifikasi konfigurasi nyata; WAL memerlukan keputusan dan pengukuran tersendiri. |
| Penjadwalan | WorkManager 2.12.0 | Library pengujian mengikuti versi runtime. |

Tabel ini mempertahankan baseline proyek, bukan mengunci semua dependensi tes. Kotlin, coroutine, KSP, Hilt, Compose testing, dan runner baru dikunci ketika kombinasi proyek berhasil diselesaikan serta dibangun.

Halaman rilis Room 3 pada tanggal pemeriksaan mencantumkan 3.0.3 sebagai stable. Gunakan artefak pengujian dan schema export yang sesuai dengan versi produksi yang dipilih. [S04]

### 6.2 Perangkat lunak pengujian

| Alat | Peran | Aturan pemakaian |
| --- | --- | --- |
| JUnit dan assertion Kotlin/JVM | Tes core serta pengujian parameter. | Pilih runner yang cocok dengan plugin proyek; tidak mengunci major baru sebelum bootstrap. |
| `kotlinx-coroutines-test` | `runTest`, dispatcher, dan scheduler waktu virtual. | Versi mengikuti coroutine produksi; satu scheduler untuk dependensi tes yang perlu sinkron. |
| Room 3 testing | Membuat database versi lama dan memvalidasi migrasi. | Gunakan `MigrationTestHelper` dari `androidx.room3.testing`. |
| WorkManager testing | Tes worker dan pengiriman pekerjaan terkontrol. | Memeriksa wiring serta delegasi; tidak menghapus guard runtime. |
| Compose UI testing | Interaksi melalui semantics dan pemeriksaan state. | Prioritaskan label/role stabil; test tag dipakai jika perlu. |
| Hilt testing | Mengganti sumber/efek melalui graph tes. | Pastikan penggantian hanya berlaku dalam varian tes. |
| Build-managed devices | Lingkungan instrumentasi yang dapat diulang. | Pilih image/API yang tersedia pada toolchain; hardware tetap diuji terpisah. |
| Macrobenchmark | Pengukuran startup, scroll, atau alur UI penting. | Jalankan pada build yang sesuai untuk benchmark, bukan memakai debug untuk menyimpulkan performa rilis. |

Referensi resmi untuk coroutine, Room, WorkManager, Compose, Hilt, perangkat terkelola, dan benchmark tersedia pada [S05] sampai [S11].

### 6.3 Kebijakan screenshot testing

Pada tanggal pemeriksaan, Compose Preview Screenshot Testing masih berstatus experimental. Dokumentasi juga menyebut peralihan dari standalone plugin ke AGP test suites pada kombinasi alpha tertentu. Krono belum mengadopsi kombinasi alpha tersebut. [S12]

Karena baseline AGP proyek berbeda, jangan menyalin konfigurasi screenshot alpha lalu menganggapnya kompatibel. Untuk tahap awal:

1. Gunakan screenshot dari instrumentasi/emulator yang lingkungan rendernya dicatat.
2. Simpan gambar acuan yang sudah diperiksa bersama kasus dan build pembuatnya.
3. Jika host screenshot tool dipilih, buat percobaan kompatibilitas tersendiri sebelum menjadikannya gate CI.
4. Periksa perubahan gambar sebelum memperbarui acuan. Pembaruan massal bukan penyelesaian otomatis terhadap layout rusak.

Screenshot dipakai untuk tampilan. Semantics, command, efek, dan aksesibilitas tetap mempunyai assertion tersendiri.

### 6.4 Repository acuan

| Repository resmi | Pemakaian | Batas penggunaan |
| --- | --- | --- |
| [android/nowinandroid](https://github.com/android/nowinandroid) | Contoh pengorganisasian aplikasi Compose dan tes. | Bukan bukti bahwa otomasi Krono bekerja. |
| [android/platform-samples](https://github.com/android/platform-samples) | Penelusuran contoh API platform. | Periksa versi serta batas contoh sebelum diadopsi. |
| [androidx/androidx](https://github.com/androidx/androidx) | Sumber API dan pengujian Jetpack. | Branch pengembangan dapat berbeda dari artefak stable. |
| [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Sumber coroutine dan test scheduler. | Gunakan tag/versi yang sesuai dengan dependensi proyek. |

Repository diperiksa sebagai sumber resmi pada 22 Oktober 2026. Commit yang diadopsi harus dicatat saat implementasi. Dokumen ini tidak mengklaim sudah membangun repository tersebut. [S18][S19][S20][S21]

## 7. Matriks perangkat dan kondisi

### 7.1 Cakupan API

Matriks ini merupakan kebutuhan lingkungan, bukan daftar perangkat yang sudah tersedia.

| Android / API | Pemeriksaan utama | Lingkungan awal |
| --- | --- | --- |
| Android 10 / 29 | Batas minimum, persistensi, wallpaper, volume, dan alur lokasi. | Emulator; perangkat fisik sebelum mengklaim dukungan penuh. |
| Android 11 / 30 | Tahapan background location dan pengembalian dari Settings. | Emulator/perangkat yang sesuai. |
| Android 12 / 31 | Bluetooth runtime permission, approximate location, PendingIntent. | Emulator untuk UI; fisik untuk TWS. |
| Android 12L / 32 | Regresi instalasi, layout, dan perilaku inti. | Emulator smoke sebelum rilis yang mencakup API ini. |
| Android 13 / 33 | Izin notifikasi dan pemilihan/impor gambar. | Emulator serta perangkat untuk efek yang tersedia. |
| Android 14 / 34 | Jenis FGS, akses, sumber gambar, dan wallpaper. | Emulator ditambah fisik sesuai fitur. |
| Android 15 / 35 | Force Stop, pemulihan registrasi, dan pekerjaan tertunda. | Emulator/perangkat. |
| Android 16 / 36 | Regresi lifecycle, sumber, efek, serta UI. | Emulator/perangkat. |
| Android 17 / 37 | Audio hardening, sesi layak, penolakan diam-diam, serta recovery. | Perangkat fisik wajib untuk klaim TWS/audio background. |

Jika sebuah API tidak diuji, tulis celah cakupannya. PASS di API yang lebih rendah dan lebih tinggi tidak otomatis membuktikan API di antaranya.

### 7.2 Cakupan perangkat fisik usulan

- Satu perangkat yang dekat dengan perilaku Android standar pada API terbaru yang didukung.
- Setidaknya satu perangkat dari keluarga vendor dengan pembatasan background yang relevan bagi calon pengguna.
- Perangkat untuk batas OS minimum bila dukungan fisik pada OS tersebut akan dinyatakan.
- TWS Classic dengan profil yang dicatat, termasuk satu perangkat pembanding bernama sama bila memungkinkan.
- Dua perangkat audio untuk memeriksa koneksi dibanding rute audio.
- TWS LE Audio hanya untuk eksperimen/dukungan LE Audio yang dinyatakan.

Model, jumlah unit yang tersedia, dan pemilik perangkat belum ditetapkan. Jangan menulis nama model sebagai telah diuji sebelum ada run nyata.

### 7.3 Matriks kondisi berjalan

| Dimensi | Kondisi yang dipilih untuk kasus relevan |
| --- | --- |
| UI | Terlihat, pindah aplikasi, layar terkunci, kembali dari Settings. |
| Proses | Hidup, dihentikan tanpa Force Stop, mulai ulang setelah kematian proses. |
| Penghentian sistem | Force Stop eksplisit, reboot, pembaruan aplikasi. |
| Daya | Normal, Doze, Battery Saver, mode vendor yang dicatat. |
| Akses | Diizinkan, ditolak, dicabut setelah aktif, akses hanya saat digunakan. |
| Sumber | Valid, kedaluwarsa, UNKNOWN, event ganda, event terlambat. |
| Koneksi | TWS pilihan, perangkat lain, dua perangkat, putus-sambung. |
| Penyimpanan | Normal, impor gagal, file hilang, commit gagal, ruang tidak cukup. |
| UI adaptif | Lebar kecil/menengah/lebar, font 100/150/200%, IME terbuka. |

Tidak perlu menjalankan seluruh perkalian kombinasi pada setiap perubahan. Gunakan kombinasi berisiko untuk tes terarah, lalu jalankan matriks dukungan wajib menjelang rilis.

Jangan menjadikan mode background “unrestricted” sebagai satu-satunya hasil. Catat konfigurasi standar terlebih dahulu; konfigurasi khusus pengguna dilaporkan sebagai skenario terpisah.

## 8. Data contoh, clock, dan observabilitas

### 8.1 Fixture dasar

| Kode fixture | Isi | Tujuan |
| --- | --- | --- |
| FX-01 | Aturan lokasi: IF Area A → gambar A; ELSE IF Area B → gambar B; ELSE opsional. | First match, UNKNOWN, perpindahan A/B. |
| FX-02 | Aturan sederhana TWS A tersambung → volume media 20%. | Identitas, episode, pemetaan volume. |
| FX-03 | Dua aturan dengan prioritas berbeda memakai `audio.media`. | Pemenang batch dan hasil pihak yang kalah. |
| FX-04 | Cabang volume + wallpaper melawan cabang yang memakai salah satu targetnya. | Konflik seluruh cabang. |
| FX-05 | Tiga aksi berurutan dengan hasil yang dapat ditahan atau dibuat gagal. | Penghentian langkah dan perubahan saat berjalan. |
| FX-06 | Aturan aktif, satu draf, dan beberapa riwayat yang namanya kemudian berubah. | Persistensi serta snapshot sejarah. |
| FX-07 | Dua aturan dan satu draf memakai file gambar yang sama. | Referensi bersama, lease, dan GC. |
| FX-08 | Database versi sebelumnya dengan pekerjaan REQUESTING, target head, dan resi command. | Migrasi dan pemulihan. |
| FX-09 | Label aturan/perangkat panjang, pesan error, angka besar, dan status gabungan. | Layout dan aksesibilitas. |

Area dan identitas pada fixture otomatis menggunakan nilai buatan. Jangan memakai rumah, lokasi rutin, atau alamat Bluetooth pengguna sebagai data contoh publik. Gambar tes berupa aset buatan dengan pola A/B yang mudah dibedakan serta hak pemakaian yang jelas.

### 8.2 Waktu dan parameter

Clock kalender dan clock monoton harus dapat digerakkan terpisah pada tes core. Usulan parameter mengikuti RULE_ENGINE.md:

| Parameter | Nilai usulan | Batas yang diuji |
| --- | --- | --- |
| Menunggu sumber Bluetooth | 30.000 ms | Sebelum, tepat pada, dan setelah deadline. |
| Menunggu sumber lokasi | 120.000 ms | Sama; callback tidak memperpanjang deadline. |
| Pemeriksaan awal | 60.000 ms | Aktivasi dan revisi aktif. |
| Command uji/retry pengguna | 15.000 ms | Command baru berbeda dari pengiriman ulang command lama. |
| Usia pengamatan geofence | 120.000 ms | Waktu sumber dan kualitas, bukan hanya waktu diterima. |
| Usia probe Bluetooth sebelum aksi | 5.000 ms | Bukan TTL yang otomatis memutus perangkat tersambung. |
| Refresh sumber otomatis | Maksimum 1 | Tidak menjadi polling tanpa batas. |
| Retry setter otomatis | 0 | Pengiriman worker ulang tidak menjadi retry efek. |

Nilai ini belum menjadi SLA pengalaman pengguna. Deadline dihitung sejak kesempatan yang sah dibuat, bukan sejak orang secara fisik melewati garis area. Keterlambatan sistem mengirim event diukur terpisah.

Pada satu runtime, gunakan waktu monoton yang sesuai dengan `elapsedRealtime`. `now >= deadline` berarti kedaluwarsa. Setelah epoch berganti, tiket lama tidak dinilai memakai angka monoton dari proses baru.

Scheduler coroutine virtual membantu menguji urutan dan delay. Clock domain tetap diinjeksi; menggerakkan scheduler saja tidak otomatis menggerakkan semua sumber waktu. Tes coroutine satu thread juga tidak membuktikan race database sungguhan. [S05]

### 8.3 Catatan yang perlu tersedia dalam build tes

Catat command ID, event/cause, episode, batch/round, rule/revision/generation, runtime epoch, plan/attempt, effect key, target intent sequence, deadline, serta alasan hasil. ID pada laporan publik dapat diganti dengan token fixture.

Bedakan catatan berikut:

1. Pembacaan state sebelum aksi.
2. Commit guard dan perubahan PREPARED → REQUESTING.
3. Masuknya pemanggilan setter.
4. Selesainya pemanggilan atau timeout pengamatan.
5. Bukti setelah aksi serta commit hasil.

Riwayat pengguna saja tidak cukup untuk menghitung setter. Build tes memerlukan recording port atau probe instrumentasi yang dapat menunjukkan jumlah dan urutan panggilan.

Hook barrier dan fault injection harus terbatas pada source set/varian tes. Build rilis tidak mempunyai tombol untuk melewati guard, mematikan deduplikasi, atau menganggap hasil palsu sebagai efek nyata.

## 9. Resep tes logika dan kontrol efek

Bagian 9 sampai 14 mendefinisikan rute tes KT-01 sampai KT-27. Setiap rute berstatus **NOT_RUN**. ID dari dokumen sebelumnya dipetakan pada bagian 18; rute di sini tidak mengganti ID asalnya.

### 9.1 KT-01: cabang, UNKNOWN, dan konfigurasi

**Lapisan:** U, C. **Prioritas:** P0.

Jalankan FX-01 dengan seluruh kombinasi A/B dari TRUE, FALSE, UNKNOWN, dengan dan tanpa ELSE. Bandingkan dengan tabel kebenaran yang ditulis terpisah dari evaluator. Ulangi dengan urutan cabang ditukar serta aturan SIMPLE memakai gate pemicu yang sah.

Berikan konfigurasi tidak valid: ELSE ganda, urutan rusak, keluarga pemicu campur, AND/OR, atau aksi belum didukung.

**Lulus jika:** A UNKNOWN memblokir B/ELSE, A TRUE tidak memerlukan kepastian B, hanya satu cabang dipilih, dan konfigurasi tidak didukung ditolak tanpa mengganti semantiknya. Kegagalan aksi cabang terpilih tidak memilih ELSE.

### 9.2 KT-02: normalisasi event dan episode

**Lapisan:** U, D, A; radio nyata pada KT-17. **Prioritas:** P0.

Gunakan urutan: koneksi valid → laporan profil ganda → UNKNOWN → tersambung lagi tanpa putus yang diketahui → putus valid → sambung valid. Untuk lokasi, gunakan A → UNKNOWN → A, lalu keluar yang diketahui dan masuk kembali.

Kirim laporan lama setelah state baru. Kirim initial check dan event sumber dengan cause berbeda tetapi episode sama. Ulangi dengan barrier agar dua masukan berdekatan.

**Lulus jika:** laporan yang sama tidak membuat efek tambahan; episode baru hanya muncul dari transisi yang sah; gap tidak dikarang menjadi putus-sambung; klaim episode menahan efek ganda lintas cause. EXIT A tidak otomatis menjadikan B FALSE.

Catat jumlah event mentah, event logis, kesempatan, klaim episode, rencana, dan setter secara terpisah. Semua jumlah itu tidak harus sama.

### 9.3 KT-03: command idempotent

**Lapisan:** U, D, C. **Prioritas:** P0.

1. Kirim aktivasi dua kali dengan command ID dan isi sama.
2. Kirim command ID sama dengan isi berbeda.
3. Ulangi untuk simpan revisi, jeda, serta uji aksi.
4. Untuk ACTION_TEST, gunakan field rule NULL dan command ID sama.
5. Kirim command pengguna baru dengan ID berbeda.

**Lulus jika:** permintaan sama mengembalikan hasil/resi sama, generasi tidak bertambah dua kali, dan ACTION_TEST tetap unik meskipun field rule NULL. Isi berbeda ditolak tanpa mengubah resi pertama. Command baru membaca guard saat itu.

### 9.4 KT-04: prioritas dan konflik seluruh cabang

**Lapisan:** U, D. **Prioritas:** P0.

Pakai FX-03 dan FX-04 dalam satu batch dari satu event kanonis. Uji dua kandidat layak memakai target sama; kandidat tinggi terhalang sebelum arbitrase; kandidat rendah mempunyai dua aksi tetapi hanya satu targetnya bertabrakan; dan wallpaper BOTH yang memerlukan dua target.

Setelah keputusan committed, buat pemenang gagal. Pada variasi lain, munculkan benturan klaim episode saat transaksi keputusan disiapkan.

**Lulus jika:** prioritas beku batch menentukan hasil, kandidat terhalang tidak mereservasi, pihak yang kalah melewati seluruh cabang, dan tidak dipromosikan setelah pemenang gagal. Benturan deduplikasi yang mengubah kandidat menyebabkan rollback serta arbitrase ulang, bukan insert diabaikan sambil mempertahankan pemenang yang salah.

Event berbeda tidak digabung menjadi satu batch hanya karena waktu kedatangannya berdekatan.

### 9.5 KT-05: guard dan klaim bersamaan

**Lapisan:** U, D, A. **Prioritas:** P0.

Buat satu rencana PREPARED. Tahan dua caller pada barrier, lalu izinkan keduanya mencoba effect key sama melalui caller/koneksi terpisah. Jangan memakai delay acak sebagai satu-satunya cara memancing race.

Ulangi dengan setiap guard tidak sah: epoch lama, recovery belum selesai, revisi/generasi berubah, prioritas berubah, deadline tercapai, sumber UNKNOWN, capability hilang, target head berbeda, target head NULL, atau langkah sebelumnya belum berhasil.

**Lulus jika:** hanya satu klaim sah dapat mencapai REQUESTING dan paling banyak satu setter dari upaya itu. Variasi tidak sah menghasilkan nol setter baru.

Uji seluruh target union. Kehilangan satu target sebelum langkah pertama menolak seluruh rencana walaupun target lain masih cocok.

### 9.6 KT-06: intent terbaru dan invoker sibuk

**Lapisan:** U, D, A. **Prioritas:** P0.

1. Tahan setter rencana lama setelah permintaan mulai.
2. Masukkan event baru dan simpan intent target lebih baru.
3. Pastikan ingress masih dapat menyimpan masukan.
4. Lepaskan setter lama; periksa hasil dan guard langkah berikutnya.
5. Tahan setter melewati timeout pengamatan, lalu ajukan rencana lain.
6. Ulangi dengan uji aksi pengguna sebagai intent terbaru.

**Lulus jika:** rencana lama yang belum mulai menjadi SUPERSEDED, langkah lanjutan tidak sah berhenti, dan Krono tidak mempunyai dua setter aktif bersamaan. Timeout pengamatan tidak membebaskan slot bila pemanggilan sebenarnya belum diketahui berakhir.

Efek yang sudah dipanggil dapat selesai setelah intent baru masuk. Tes tidak menuntut pembatalan atomik terhadap Android atau aplikasi lain.

### 9.7 KT-07: perubahan pengguna saat berjalan

**Lapisan:** U, D, C, H. **Prioritas:** P0.

Pakai FX-05. Setelah aksi pertama APPLIED, tahan sebelum klaim aksi kedua. Lakukan jeda, hapus, revisi, atau reorder prioritas pada variasi terpisah.

Setelah kesempatan selesai, ubah volume/wallpaper manual. Buka daftar, detail, dan kembali dari background tanpa event baru.

**Lulus jika:** langkah yang belum dimulai dari kontrol lama dihentikan; hasil aksi pertama tetap dicatat; tidak ada rollback buatan. Prioritas baru tidak menjalankan semua aturan. Perubahan manual dan pembacaan UI tidak menciptakan kesempatan efek baru.

Revisi aktif yang disimpan boleh mempunyai initial check baru dari command sah. Bedakan pemeriksaan baru itu dari pekerjaan lama yang dibatalkan.

### 9.8 KT-08: deadline, kelanjutan, dan retry

**Lapisan:** U, D. **Prioritas:** P0.

Uji `deadline - 1 ms`, `deadline`, dan `deadline + 1 ms`. Geser jam kalender maju/mundur tanpa menggerakkan clock monoton. Gerakkan waktu monoton saat UI diam atau perangkat disimulasikan tidur.

Pulihkan capability/data sebelum deadline dan setelah CLOSED. Kirim ulang worker, callback, serta periksa kesiapan. Buat USER_RETRY baru secara eksplisit.

**Lulus jika:** tepat deadline sudah kedaluwarsa; deadline tidak diperpanjang; kelanjutan sah memakai snapshot/round baru dengan cause dan deadline lama. CLOSED tidak terbuka kembali. Retry pengguna mempunyai command baru dan membaca keadaan terkini; refresh kesiapan tidak membuat kesempatan retry baru.

## 10. Resep tes penyimpanan dan pemulihan

### 10.1 KT-09: transaksi, constraint, dan atomicity

**Lapisan:** D. **Prioritas:** P0.

Untuk TX-01 sampai TX-08, siapkan keadaan sebelum, sisipkan kegagalan di titik relevan, lalu buka koneksi baru dan periksa data committed pada Room nyata.

| Transaksi | Pemeriksaan utama |
| --- | --- |
| TX-01 | Revisi, pointer, generasi, resi, initial check, dan dispatch request tidak terpisah sebagian. |
| TX-02 | Jeda/hapus membatalkan langkah lama serta menjaga hasil yang sedang berlangsung. |
| TX-03 | Reorder memakai posisi sementara yang sah; posisi final unik dan versi naik. |
| TX-04 | Fakta, coverage, episode, dan event/resume request konsisten. |
| TX-05 | Keputusan, klaim episode, rencana, target, attempt, dan outbox committed bersama. |
| TX-06 | Guard/CAS REQUESTING committed sebelum izin API diberikan. |
| TX-07 | Hasil, agregat, marker proyeksi, dan history konsisten. |
| TX-08 | Epoch baru memfence pekerjaan lama sebelum mengizinkan efek baru. |

Periksa FK lintas revisi, unique key dengan NULL, row version/CAS, kenaikan `control_version` untuk perubahan kelayakan, serta counter tanpa overflow diam-diam. Preferensi tampilan tidak menjadi otoritas eksekusi.

**Lulus jika:** commit gagal tidak meninggalkan konfigurasi setengah jadi; constraint DB benar-benar ditegakkan database; validator aplikasi menutup aturan yang tidak dapat dinyatakan oleh constraint.

Setter, pekerjaan eksternal, dan pembacaan file panjang tidak dijalankan dalam transaksi SQL. INSERT OR REPLACE tidak dipakai untuk menyelesaikan benturan record kontrol.

### 10.2 KT-10: crash pada batas jurnal efek

**Lapisan:** U, D, A, E. **Prioritas:** P0.

Gunakan injeksi kegagalan pada setiap batas dan penghentian proses nyata pada build tes yang kemudian membuka database sama. Pembatalan coroutine saja bukan bukti kematian proses.

| Titik penghentian | Hasil setelah runtime baru pulih |
| --- | --- |
| Sebelum commit PREPARED | Tidak muncul setter dari record yang tidak pernah sah. |
| Setelah PREPARED, sebelum klaim | Pekerjaan epoch lama dibatalkan. |
| Setelah commit REQUESTING, sebelum API | Tetap berpotensi sudah diminta; tidak diulang buta. |
| Setelah API masuk, sebelum hasil diketahui | UNVERIFIED bila bukti tidak cukup; tidak mengulang setter. |
| Saat OBSERVING | Efek yang belum pasti diperlakukan konservatif. |
| Setelah FINISHED committed | Hasil lama dipakai; worker ulang tidak memanggil setter. |
| Saat WAITING_DATA/CAPABILITY | Tiket epoch lama diakhiri; pemantauan direkonsiliasi. |

**Lulus jika:** epoch baru difence sebelum efek, catatan lama tidak menjadi PREPARED baru, dan kecocokan nilai pada readonly probe tidak dianggap bukti bahwa setter Krono membuatnya.

Ada celah antara commit database dan API Android. Tes tidak menyatakan adanya transaksi atomik DB+OS atau jaminan exactly once universal.

### 10.3 KT-11: outbox, worker, dan graph runtime

**Lapisan:** D, A, E. **Prioritas:** P0.

Buat kegagalan sebelum enqueue, setelah enqueue sebelum ACK, serta saat worker dikirim ulang. Worker menerima ID referensi dan membaca otoritas dari database.

Uji referensi yang terminal, sudah dipadatkan secara sah, atau berasal dari epoch lama. Jalankan receiver, UI, worker, dan sesi layanan melalui graph aplikasi tes.

**Lulus jika:** dispatch tertinggal direkonsiliasi tanpa kesempatan baru; enqueue bukan setter; referensi hilang/stale selesai aman. Semua jalur memakai coordinator/invoker sama dalam satu proses.

Tes WorkManager memeriksa delegasi, constraint pengiriman, dan initializer/WorkerFactory. TestDriver tidak membuktikan callback sistem tepat waktu atau memberi hak audio background. [S07]

### 10.4 KT-12: history, watermark, retensi, dan tombstone

**Lapisan:** D, C, E. **Prioritas:** P0.

1. Buat history beberapa aturan dan satu aksi yang ditahan.
2. Hapus semua history, lalu selesaikan aksi dari event sebelum cutoff.
3. Kirim ulang worker/callback lama.
4. Ulangi penghapusan untuk satu aturan.
5. Biarkan retensi menghapus record dengan proyeksi WRITTEN.
6. Hapus aturan sambil mempertahankan history, lalu buka detail lama.
7. Jalankan compaction sah, lalu kirim referensi worker lama.

**Lulus jika:** history lama tidak muncul kembali; hasil lama menjadi SUPPRESSED sesuai watermark; marker WRITTEN mencegah kebangkitan setelah retensi. Aturan, deduplikasi, target barrier, dan resi minimal tetap sah.

Default 30 hari/10.000 dan opsi 7/30/90 mengikuti Database/Design. Uji batas waktu serta jumlah terpisah, termasuk waktu sama dan pagination stabil. Snapshot nama/hasil terbaca setelah revisi atau penghapusan aturan.

### 10.5 KT-13: impor gambar, akses file, dan GC

**Lapisan:** D, A, C, H. **Prioritas:** P1; race kontrol/file menjadi P0.

Pilih gambar lokal dan cloud melalui Photo Picker. Tahan impor, batalkan, buat baca gagal, buat ruang tidak cukup, serta hentikan proses sebelum dan setelah file final dibuat.

Jalankan FX-07. Tahan lease baca saat GC mencoba menghapus; pada variasi lain, GC telah mengklaim DELETING sebelum pembaca baru datang.

**Lulus jika:** READY hanya setelah file privat final dapat dibaca; kegagalan tidak mengganti pilihan lama yang sah; hilangnya thumbnail tidak menghapus asli. Referensi bersama/draf/attempt/lease menjaga file; DELETING menolak referensi atau lease baru secara atomik.

Uji setelah reboot dan setelah URI sumber tidak tersedia. Jalur produksi usulan memakai impor privat, sehingga file siap tidak bergantung pada URI sumber yang kebetulan masih berlaku.

### 10.6 KT-14: schema, migrasi, dan SQLite

**Lapisan:** D, E. **Prioritas:** P0.

Sebelum versi pertama, validasi schema export, indeks/FK, tipe waktu, query inti, reopen file database, dan journal mode TRUNCATE yang dipakai.

Ketika migrasi pertama tersedia:

1. Buat database dari schema lama yang benar-benar pernah dirilis.
2. Isi aturan, draf, aset bersama, history, resi, klaim episode, target head, dan REQUESTING yang belum pasti.
3. Jalankan migrasi ke versi terbaru menggunakan Room 3 MigrationTestHelper.
4. Validasi struktur dan isi data dengan assertion bisnis.
5. Buka runtime baru dan pulihkan tanpa setter ulang.
6. Uji seluruh versi lama yang masih didukung, termasuk jalur bertahap.

**Lulus jika:** konfigurasi serta kontrol penting dipertahankan, tidak ada duplikasi atau destructive fallback. Migrasi gagal tidak diganti dengan penghapusan database diam-diam.

Schema 1 → 2 pada rancangan hanya contoh. Belum ada migrasi produksi yang dapat dinyatakan lulus. MigrationTestHelper membantu memvalidasi schema/migrasi; isi bisnis tetap memerlukan assertion proyek. [S06]

## 11. Resep tes kemampuan Android

### 11.1 KT-15: akses lokasi dan registrasi pemantauan

**Lapisan:** A, C, H. **Prioritas:** P1; status sukses palsu atau efek tanpa kondisi sah menjadi P0.

Uji akses lokasi ditolak, approximate saja, precise, hanya saat digunakan, background ditolak, serta dicabut setelah aturan aktif. Jalankan alur grant melalui UI sistem yang berlaku pada masing-masing API.

Periksa bahwa akses Bluetooth saja cukup untuk alur TWS yang tidak menggunakan lokasi. Jangan menerapkan pembatasan manifest contoh Bluetooth yang merusak kebutuhan GPS Krono.

Untuk registrasi area, uji sumber bersama antaraturan, jumlah mendekati kapasitas, kegagalan layanan lokasi, pembaruan versi sumber, dan rekonfigurasi setelah boot/update.

**Lulus jika:** kesiapan membedakan akses, sumber, dan status registrasi. Keinginan aktif tidak disamakan dengan registrasi berhasil. Sumber bersama dihitung secara benar; kapasitas berlebih dijelaskan dan tidak menghasilkan subset pemantauan yang diam-diam dianggap lengkap.

Geofencing membatasi jumlah area per aplikasi/pengguna. Pastikan perhitungan kapasitas mengikuti registrasi yang sebenarnya, bukan jumlah kartu aturan. PendingIntent serta receiver memakai konfigurasi yang sesuai API dan komponen yang eksplisit. [S14]

### 11.2 KT-16: area A/B, overlap, dan kualitas lokasi

**Lapisan:** U, A, H. **Prioritas:** P0 untuk logika; P1 untuk karakterisasi pengiriman.

Lakukan dua jenis run:

1. **Run terkontrol:** kirim pengamatan valid melalui port tes untuk membuktikan A → B tetap dievaluasi, termasuk ketika berada pada kawasan besar yang sama.
2. **Run lapangan:** tentukan area nyata beradius yang dicatat, jalani rute A → luar → B dan rute yang melewati overlap, lalu rekam event sistem serta kualitas data.

Periksa variasi berikut:

- A TRUE/B TRUE memilih cabang pertama.
- A UNKNOWN/B TRUE menunggu; B tidak langsung dipilih.
- EXIT A tanpa pengamatan B yang layak menjaga B UNKNOWN.
- Pengamatan lama tiba belakangan dan tidak menjadi segar hanya karena baru diterima.
- Refresh data yang dibatasi tidak menciptakan episode masuk baru.
- A → UNKNOWN → A tidak memasang ulang tanpa transisi sah.

Untuk heuristik lokasi usulan, uji batas `d + a <= r`, `d - a > r`, dan daerah ketidakpastian di antaranya. Nilai akurasi yang dilaporkan tetap bukan jaminan posisi fisik pasti.

**Lulus jika:** hasil cabang dan efek sesuai input yang sah. Untuk run lapangan, catat apakah event diterima, kapan, dan kondisi akses/dayanya. Panduan Android menyebut pengiriman geofence dapat tertunda; jangan menetapkan “ganti dalam satu detik” sebagai kriteria buatan. [S14]

Uji jaringan hilang sebagai kondisi tersendiri. Dua alur inti tidak membuat geofencing otomatis dijamin sepenuhnya offline.

### 11.3 KT-17: identitas dan kejadian TWS fisik

**Lapisan:** A, H. **Prioritas:** P1; efek untuk perangkat salah atau ganda menjadi P0.

1. Pilih TWS A melalui daftar perangkat yang dapat diakses.
2. Sambungkan A, kemudian perangkat B pada run terpisah.
3. Gunakan dua nama tampilan sama jika unit tersedia.
4. Catat profil A2DP/headset yang melapor dalam satu episode.
5. Putus-sambungkan secara sah; uji satu earbud dan kedua earbud sesuai transport.
6. Ulangi saat proses tidak hidup melalui penghentian yang bukan Force Stop.
7. Uji LE Audio dalam run terpisah sebelum menyatakan dukungannya.

**Lulus jika:** identitas perangkat tidak bergantung pada nama saja; perubahan perangkat lain tidak memicu aturan A; laporan profil ganda tidak menghasilkan efek ganda. Profil/transport yang tidak dapat dinormalisasi secara andal ditandai belum didukung atau terhalang.

Catat koneksi dan rute audio aktif sebagai dua hal berbeda. Berhasil menerima event tidak membuktikan volume dapat ditulis. Berhasil mengatur volume tidak membuktikan volume tersebut khusus untuk satu TWS.

### 11.4 KT-18: gerbang audio Android 17

**Lapisan:** A, H. **Prioritas:** P0 untuk pelaporan/guard; P1 untuk pembuktian kemampuan fitur.

Ini adalah PoC wajib sebelum menjanjikan otomasi volume background pada Android 17. Gunakan target SDK 37, pembatasan sistem normal, TWS fisik, dan build yang identitasnya dicatat.

| Keadaan | Pemeriksaan |
| --- | --- |
| Activity Krono terlihat | Permintaan volume sah dan bukti hasil diperiksa. |
| Receiver biasa tanpa sesi layak | Produksi menolak/menunggu sesuai guard; tidak menganggap tidak ada exception sebagai sukses. |
| Worker biasa tanpa sesi layak | Worker tidak dianggap mendapat hak audio karena dapat berjalan. |
| FGS tanpa WIU yang diperlukan | Hambatan terlihat pada capability/diagnostik; tidak dilabeli APPLIED. |
| Sesi dimulai pengguna saat alur sah terlihat | Jenis layanan, kelayakan WIU, perpindahan ke background, dan hasil aktual dibuktikan. |
| Activity hilang atau layar terkunci | Kelayakan dinilai kembali; hasil foreground tidak disalin menjadi bukti background. |
| Sesi dihentikan pengguna | Permintaan baru tidak memakai capability sesi lama. |
| Proses mati/reboot | Sesi sebelumnya tidak dianggap hidup kembali atau otomatis memperoleh WIU. |

Android 17 membatasi interaksi audio background. Aplikasi yang menargetkan API 37 memerlukan konteks WIU yang sesuai ketika berjalan di background; setter dapat gagal diam-diam. Ini menjadi alasan adanya pemeriksaan capability dan hasil, bukan sekadar exception handling. [S13]

Untuk membuktikan pembatasan platform yang diam, gunakan harness PoC terisolasi bila jalur produksi sudah menolak sebelum API. Harness hanya untuk observasi, bukan jalur bypass produksi.

Catat `AudioHardening` pada logcat serta informasi `dumpsys audio` yang relevan. Diagnostik resmi membedakan penolakan karena tidak ada FGS dan karena tidak ada kemampuan WIU. [S13]

**Lulus untuk penanganan hambatan jika:** Krono tidak menulis status sukses palsu, tidak mencoba setter berulang, dan menyediakan penjelasan sesuai keadaan.

**Lulus untuk kemampuan otomasi jika:** event TWS asli, sesi yang sah, guard, setter, dan bukti hasil bekerja saat UI Krono tidak terlihat pada kombinasi yang dinyatakan didukung. Kedua jenis kelulusan dilaporkan terpisah.

Jenis FGS harus sesuai penggunaan yang benar-benar sah. `connectedDevice` hanya kandidat yang perlu dibuktikan. Jangan mengklaim keberhasilan memakai layanan pemutaran/mikrofon/alarm palsu atau pembatasan sistem yang dimatikan.

Pastikan Activity tes atau instrumentation tidak tanpa sengaja membuat UID dianggap foreground atau memberi konteks yang tidak tersedia pada penggunaan nyata. Untuk run background penentu dukungan, gunakan alur pengguna biasa dan observasi pasif; catat bila harness memengaruhi lifecycle.

### 11.5 KT-19: hasil setter volume dan wallpaper

**Lapisan:** U, A, H. **Prioritas:** P0 untuk klasifikasi hasil; P1 untuk dukungan sasaran.

**Volume media:**

- Uji target 0%, 1%, 20%, 50%, 99%, 100%, serta nilai yang jatuh di antara tingkat sistem.
- Catat rentang/index volume sebelum, target hasil pemetaan, dan index sesudah.
- Uji nilai sudah sesuai: NO_CHANGE_NEEDED dengan nol setter.
- Buat fake setter tidak melempar exception tetapi tidak mengubah hasil.
- Uji fixed-volume atau batas perangkat bila tersedia.
- Uji dua perangkat tersambung, perpindahan rute, dan perubahan manual setelah selesai.

Nilai persen dipetakan ke tingkat yang tersedia. PASS bukan klaim ukuran keras suara fisik sama pada semua TWS.

**Wallpaper:**

- Uji layar utama, layar kunci, dan keduanya pada sasaran yang tersedia.
- Gunakan gambar A/B yang mudah dibedakan; periksa tampilan aktual dan hasil API yang tersedia.
- Uji dukungan/policy yang menolak, file hilang/rusak, serta hasil ID pemasangan 0.
- Uji rasio gambar, crop/preview, dan hasil setelah layar dikunci/dibuka.
- Jangan mengandalkan pembacaan wallpaper lama yang dibatasi OS untuk membuat pembanding atau rollback otomatis.

**Lulus jika:** status mengikuti kekuatan bukti. APPLIED, NO_CHANGE_NEEDED, FAILED, BLOCKED, dan UNVERIFIED dibedakan. Ketika aksi pertama APPLIED lalu aksi berikutnya gagal, hasil Sebagian berhasil dan langkah sisanya tidak dijalankan.

Aplikasi atau pengguna lain dapat mengubah nilai setelah aksi. Catat interval observasi dan gangguan eksternal; kecocokan saat dibaca belakangan saja tidak selalu membuktikan efek Krono.

**Fixture agregat final, dinilai setelah kesempatan CLOSED:**

| Hasil per aksi / keputusan | Ringkasan yang diharapkan |
| --- | --- |
| Tidak ada cabang/aksi terpilih | Dilewati dengan alasan. |
| Semua APPLIED atau NO_CHANGE_NEEDED | Berhasil; rincian membedakan perubahan dan nilai yang sudah sesuai. |
| APPLIED lalu FAILED, CANCELLED, atau UNVERIFIED | Sebagian berhasil. |
| NO_CHANGE_NEEDED lalu UNVERIFIED, tanpa APPLIED | Belum terverifikasi. |
| FAILED dan UNVERIFIED, tanpa APPLIED | Belum terverifikasi. |
| NO_CHANGE_NEEDED lalu FAILED, tanpa APPLIED/UNVERIFIED | Gagal. |
| Tidak ada APPLIED/UNVERIFIED/FAILED dan terdapat BLOCKED | Terhalang. |
| Seluruh pekerjaan dilewati/dibatalkan/kedaluwarsa | Dilewati dengan alasan khusus. |

Tambahkan pemeriksaan bahwa kesempatan belum CLOSED tidak diproyeksikan sebagai hasil final Berhasil. NO_CHANGE_NEEDED saja tidak membuktikan sebagian perubahan baru telah diterapkan.

### 11.6 KT-20: reboot, Force Stop, daya, dan pemulihan

**Lapisan:** A, E, H. **Prioritas:** P0 untuk tidak mengulang efek; P1 untuk pemantauan.

Pisahkan run berikut: pindah aplikasi, membuang task dari Recent Apps, kematian proses tanpa Force Stop, Force Stop dari Settings, reboot, serta update aplikasi yang mempertahankan data. Jangan menggunakan satu tindakan sebagai pengganti semuanya.

Pada tiap run, catat keadaan proses/registrasi/sesi sebelum, tindakan yang dilakukan, dan langkah pengguna yang diperlukan sesudahnya. Lifecycle proses Android dapat berubah ketika komponen tidak lagi aktif; tes tidak boleh bergantung pada callback penutupan yang pasti terjadi. [S15]

**Lulus jika:** konfigurasi tetap ada, recovery memfence pekerjaan lama, dan pemantauan direkonsiliasi sesuai akses serta dukungan. Force Stop tidak diiklankan sebagai keadaan yang tetap menerima semua event. Setelah pengguna kembali, registrasi dan readiness diperiksa ulang tanpa replay efek lama.

Ulangi kondisi terpilih pada Doze, layar terkunci, Battery Saver, serta pembatasan vendor. Ukur delay dan kejadian yang tidak diterima. Jangan menjadikan “tidak crash” sebagai satu-satunya hasil.

Penolakan notifikasi diuji terpisah dari izin lokasi/Bluetooth. Jika FGS dipakai, syarat notifikasi dan lifecycle layanan tetap dinilai sesuai keadaan; menolak notifikasi tidak berarti seluruh fitur otomatis mati.

## 12. Resep tes tampilan dan interaksi

### 12.1 KT-21: layar, validasi, dan model status

**Lapisan:** C, E. **Prioritas:** P1; command/efek salah menjadi P0.

Periksa 12 konteks layar dalam DESIGN.md: Aturan, Editor, Pemilih Area, Pemilih TWS, Pengaturan Aksi, Tinjau, Simulasi, Uji Aksi, Riwayat, Detail Hasil, Kesiapan, dan Pengaturan.

Pada layar relevan, periksa state kosong, memuat, isi tersedia, error, akses hilang, dan pekerjaan berjalan. Tidak semua layar harus mempunyai semua state bila tidak bermakna.

**Lulus jika:**

- Keinginan aktif, kesiapan, pekerjaan berjalan, dan hasil terakhir tidak digabung menjadi satu status.
- Draf tidak mengubah revisi aktif atau pengaturan HP.
- TWS bernama sama dapat dibedakan dan impor gagal tidak tampil READY.
- Simpan/aktivasi menjelaskan initial check; jeda tidak menjanjikan pemulihan pengaturan.
- Urutan cabang/aksi terlihat dan tidak berubah akibat optimasi tersembunyi.
- Prioritas yang diedit adalah urutan global meskipun daftar sebelumnya difilter.
- Riwayat aturan terhapus tetap terbaca; tautan yang tidak lagi sah tidak ditawarkan.
- Fitur yang belum didukung tidak muncul sebagai katalog aksi siap pakai.

Gunakan semantics dan fake repository/runtime untuk menguji state. Tambahkan satu alur terintegrasi dengan penyimpanan nyata agar wiring screen tidak hanya benar terhadap fake.

Untuk area yang dipakai bersama, periksa ringkasan aturan terdampak serta pilihan membuat salinan. Mengedit draf tidak mengubah area aktif untuk aturan lain secara diam-diam.

Lakukan sesi keterpahaman dengan calon pengguna setelah alur dapat dipakai. Minta mereka membuat contoh TWS dan area A/B, lalu menjelaskan apa yang terjadi saat diaktifkan, saat data UNKNOWN, serta setelah perubahan manual. Jangan memberi jawaban sebelum mereka menjelaskan. Catat jumlah peserta, kesulitan, dan salah paham; hasilnya belum mewakili populasi pengguna secara statistik.

### 12.2 KT-22: simulasi, kesiapan, retry, dan uji nyata

**Lapisan:** U, C, E, H. **Prioritas:** P0.

Jalankan empat tindakan dari keadaan sama:

| Tindakan | Efek yang boleh terjadi |
| --- | --- |
| Simulasi | Hanya keputusan pada input contoh; nol efek dan mutasi kontrol produksi. |
| Periksa kesiapan | Probe terbatas; boleh melanjutkan tiket lama yang masih sah sesuai engine, tanpa membuat retry baru. |
| Coba aturan sekarang | USER_RETRY baru; baca aturan/kondisi terkini, aksi dapat berjalan jika sah. |
| Uji aksi | USER_TEST_ACTION langsung untuk aksi terpilih, dengan guard kemampuan dan peringatan efek nyata. |

**Lulus jika:** label/CTA menjelaskan perbedaannya, command serta hasil diberi asal yang benar, dan simulasi yang drafnya berubah ditandai perlu dihitung ulang. Simulasi satu aturan tidak mengklaim konflik antaraturan sudah diperiksa.

Uji aksi dari draf yang lengkap tidak memalsukan keadaan lokasi atau koneksi agar dianggap cocok. Uji wallpaper/volume nyata tidak dihitung sebagai bukti penerimaan pemicu background.

### 12.3 KT-23: rotasi, navigasi ulang, dan command pending

**Lapisan:** C, E, D. **Prioritas:** P0.

Tahan simpan, aktivasi, atau uji aksi dalam keadaan pending. Ketuk berulang, rotasi layar, ubah ukuran jendela, pindah halaman, dan buka kembali. Pada run terpisah, matikan proses setelah command diterima.

**Lulus jika:** UI memakai resi/command ID yang sama untuk permintaan yang sama, tidak membuat setter atau generasi baru, dan menampilkan hasil yang tersimpan setelah tersambung kembali. Draf serta field valid tetap dipulihkan sesuai kontrak.

Refresh Flow tidak menghapus nama yang diketik, scroll, fokus penting, atau sheet yang masih berlaku. Jika command belum pernah diterima dan data belum tersimpan, UI tidak boleh menyatakan berhasil hanya karena halaman berganti.

Sinkronisasi tes Compose dipakai untuk pekerjaan UI yang dapat diamati. Animasi, coroutine, database, dan clock domain tidak otomatis berbagi satu waktu; tunggu kondisi yang relevan atau gerakkan clock yang benar, tanpa sleep tetap sebagai pengganti assertion. [S17]

### 12.4 KT-24: aksesibilitas

**Lapisan:** C, E, H. **Prioritas:** P1; hambatan alur inti menjadi High.

Periksa otomatis melalui semantics, lalu uji manual dengan TalkBack dan keyboard. Pemeriksaan otomatis melengkapi observasi layanan aksesibilitas, bukan menggantikannya. [S16]

Checklist utama:

- Kontrol mempunyai label, role, state, dan nilai yang tepat.
- Switch menyatakan aturan mana dan keadaan aktifnya.
- Target sentuh minimal 48 × 48dp tanpa tumpang tindih.
- Fokus mengikuti urutan logika dan kembali setelah sheet ditutup.
- Reorder dapat dilakukan lewat tindakan “pindahkan ke atas/bawah”, tidak hanya drag.
- Slider menyediakan input angka; error terkait field dapat dibaca.
- Status memakai teks/ikon selain warna.
- Informasi penting terbaca pada font 200%; label pembeda tidak hilang.
- Pengumuman hasil tidak membanjiri setiap callback atau progres dekoratif.
- Preferensi pengurangan animasi tidak menghilangkan feedback atau akses kontrol.

Kontras diperiksa pada pasangan warna dan state yang benar-benar dirender: normal, pressed, disabled, fokus, warning, dan error. Hasil perhitungan token pada DESIGN.md bukan sertifikat aksesibilitas seluruh aplikasi.

### 12.5 KT-25: layout adaptif dan acuan visual

**Lapisan:** C, E, H. **Prioritas:** P1; detail dekoratif P2.

Gunakan lingkungan render tetap dengan ukuran lebar contoh 360, 600, 840, dan 960dp, serta titik di sekitar batas layout yang dipilih. Uji font 100%, 150%, 200%, orientasi/tinggi terbatas, insets, dan IME terbuka.

Pilih screenshot state yang bermakna: daftar kosong, aturan aktif terhalang, editor cabang panjang, impor gagal, tinjau, uji berjalan, hasil sebagian berhasil, UNVERIFIED, dialog hapus, dan Settings.

**Lulus jika:** field, parameter, CTA, serta pesan error dapat dicapai; tidak ada scroll horizontal halaman yang memotong alur inti; navigation rail/panel mengikuti ruang jendela; urutan makna tidak berubah. Editor preview tambahan pada jendela lebar tidak membuat command baru.

Jangan membuat seluruh perkalian layar × ukuran × font sebagai acuan tanpa alasan. Pilih variasi paling berisiko untuk regresi otomatis, lalu jelajahi kombinasi lain secara manual.

Simpan API image, density, font, locale, theme, dan build bersama gambar acuan. Mask hanya daerah yang memang nondeterministik, bukan bagian bermasalah yang seharusnya diperbaiki.

## 13. KT-26: performa dan konsumsi sumber daya

**Lapisan:** U, D, E, H. **Prioritas:** P1; kehilangan event/kontrol atau efek ganda menjadi P0.

### 13.1 Dataset dan pengukuran awal

| Dataset | Isi usulan |
| --- | --- |
| Ringan | 0 sampai 10 aturan, sedikit history, satu aset per aksi wallpaper. |
| Menengah | 100 aturan dengan sumber bersama yang tetap dalam kapasitas registrasi. |
| Riwayat penuh | Sampai 10.000 hasil dengan pagination dan campuran kategori. |
| Gambar menantang | Dimensi besar, rasio berbeda, decode gagal, dan beberapa impor berdekatan. |
| Event burst | Duplikasi, profil ganda, serta event baru saat setter ditahan. |

Jumlah ini adalah dataset tes, bukan janji batas produk atau jumlah geofence yang boleh diregistrasikan.

Ukur startup, membuka daftar/editor, scroll history, simpan revisi, evaluasi batch, waktu transaksi, antrean outbox, memori, serta decoding gambar. Pisahkan waktu aplikasi memproses event dari keterlambatan sistem mengirim event.

Catat median/p95 bila jumlah sampel memadai, jumlah run, kondisi cold/warm, serta variasinya. Jangan menampilkan angka p95 dari satu percobaan.

### 13.2 Pengukuran perangkat

Gunakan build dan lingkungan benchmark yang sesuai. Macrobenchmark dipakai untuk alur startup/UI yang dipilih; pengukuran core/SQL yang lebih kecil boleh memakai alat yang lebih terarah. [S11]

Bandingkan kondisi idle tanpa aturan, aturan aktif menunggu, koneksi TWS, dan lokasi aktif. Catat lama pengamatan, baterai/charger, jaringan, layar, layanan, serta aplikasi lain yang relevan.

**Lulus awal jika:** tidak ada ANR/crash pada alur terpilih, tidak ada polling atau retry tanpa batas, data/history tetap dibatasi, dan proses evaluasi tidak menutup ingress ketika setter menunggu.

Target numerik waktu, memori, serta daya belum ditetapkan. Ambil baseline pertama, usulkan anggaran per metrik, lalu sepakati sebelum gate performa kuantitatif dipakai. Angka “hemat baterai” atau “selalu instan” tidak boleh dibuat dari rancangan ini.

## 14. KT-27: data lokal, backup, dan pemisahan build tes

**Lapisan:** D, A, E, H. **Prioritas:** P1; kehilangan kontrol atau pemulihan efek lama menjadi P0.

Periksa bahwa kebutuhan inti tidak memaksa akun/backend. Tes penolakan jaringan tetap membedakan penyimpanan lokal dari sumber gambar cloud atau layanan lokasi yang membutuhkan jaringan.

Pemeriksaan data mengikuti Architecture dan Database:

- Aset asli berada di penyimpanan privat yang dipilih, bukan hanya cache sementara.
- Log serta laporan default tidak menampilkan koordinat/identitas perangkat yang tidak diperlukan.
- Hapus riwayat tidak diam-diam menghapus aturan atau mengubah HP.
- Build rilis tidak memuat hook injeksi efek palsu atau bypass guard.
- Aturan backup/data extraction sesuai keputusan proyek dan diuji dengan restore/transfer yang tersedia.
- Data hasil restore/transfer tidak langsung dipercaya sebagai runtime epoch atau sesi audio yang masih hidup.

**Lulus jika:** hasil mengikuti kebijakan yang benar-benar dikonfigurasi dan kontrol eksekusi tetap konservatif setelah restore. `allowBackup=false` saja tidak dipakai sebagai bukti semua bentuk transfer data vendor sudah terblokir. [S22]

Jika jalur transfer tertentu tidak tersedia untuk pengujian, catat BLOCKED dan batas klaimnya. Tes ini bukan alasan menambah akun, enkripsi khusus, atau mekanisme ekspor yang belum masuk cakupan.

## 15. Urutan pelaksanaan dan CI

### 15.1 Tahap yang dapat dikerjakan bertahap

| Tahap | Pekerjaan | Syarat selesai |
| --- | --- | --- |
| A. Bootstrap | Kunci kombinasi toolchain; buat source set, runner, graph tes, schema export, dan pencatatan hasil. | Build pertama serta satu tes bermakna per batas utama berhasil dijalankan. |
| B. Core | KT-01 sampai KT-08 dengan clock/fake terkontrol. | Cabang, episode, konflik, guard, dan waktu teruji tanpa Android. |
| C. Data | KT-09 sampai KT-14 dengan Room/driver nyata. | Atomicity, crash/reopen, deduplikasi, history, serta aset teruji. |
| D. Platform PoC | KT-15 sampai KT-20, dahulukan KT-18. | Kemampuan lokasi/TWS/efek mempunyai bukti dan batas dukungan tertulis. |
| E. UI | KT-21 sampai KT-25. | Alur inti, semantics, layout, dan command tersambung ke kontrak runtime. |
| F. Kualitas rilis | KT-26, KT-27, matriks dukungan, serta AC-01 sampai AC-13. | Gate bagian 17 dinilai terhadap satu kandidat build. |

PoC audio Android 17 dikerjakan awal setelah harness minimum tersedia, paralel secara urutan pengembangan dengan pekerjaan yang tidak bergantung pada hasilnya. Jangan menunggu seluruh UI selesai untuk mengetahui bahwa jalur inti belum layak.

Tabel ini mengurutkan pembuktian. Pembagian pekerjaan pengembangan rinci akan ditempatkan pada IMPLEMENTATION_PLAN.md nomor 8.

### 15.2 Pilihan suite berdasarkan perubahan

| Jenis perubahan | Pemeriksaan minimum yang relevan |
| --- | --- |
| Evaluator/semantik | Seluruh suite core terkait cabang, episode, konflik, dan hasil. |
| Coordinator/guard/jurnal | Suite core dan integrasi race, claim, pemulihan, serta outbox. |
| Schema/query/migrasi | Integrasi Room, constraint, jalur migrasi yang berlaku, dan reopen/recovery. |
| Adapter lokasi/Bluetooth/audio | Tes pemetaan/failure ditambah run perangkat yang terkena perubahan. |
| Command UI/navigasi | Component test serta satu alur terintegrasi untuk duplikasi/lifecycle. |
| Token/layout | Screenshot terpilih, skala font, fokus, serta interaksi terdampak. |
| Dokumentasi saja | Konsistensi ID, tautan, dan kontrak; tidak perlu mengulang semua tes perangkat. |
| Pembaruan dependensi | Build graph serta suite pada batas API yang berubah. |
| Kandidat rilis | Seluruh kasus wajib yang berlaku dan matriks dukungan yang diumumkan. |

Suite emulator yang lebih luas dapat berjalan pada CI berkala atau sebelum rilis. Tes fisik tetap dijadwalkan sebagai pekerjaan tim; tidak dinyatakan sudah diotomatisasi hanya karena tercantum dalam rencana.

### 15.3 Perintah sebagai contoh, belum dijalankan

Belum ada proyek Gradle Krono pada tahap ini. Perintah berikut adalah pola awal setelah modul serta varian pada Architecture dibuat, bukan bukti task tersebut sudah tersedia.

```bash
./gradlew :app:tasks --all
./gradlew :core:engine:test :core:runtime:test
./gradlew :data:local:connectedDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

Nama task aktual diperiksa saat bootstrap. Sesuaikan varian, source set, serta konfigurasi perangkat terkelola dengan plugin yang benar-benar dipakai. Jalankan task untuk modul adapter juga ketika tes Android ditempatkan di sana.

CI menyimpan commit/build ID, versi dependensi, laporan tes, log yang relevan, serta gambar acuan/diff jika ada. Kegagalan tidak ditutup dengan menjalankan ulang sampai kebetulan hijau.

### 15.4 Tes nondeterministik

Jika sebuah tes kadang gagal, simpan hasil gagal beserta seed/urutan kejadian. Bedakan masalah test harness, environment, dan bug race produk.

Memindahkan tes ke daftar karantina memerlukan alasan, penanggung jawab, serta pekerjaan pengganti yang menjaga cakupan. Kasus P0 yang belum dapat dibuktikan tetap menjadi celah kelulusan; karantina tidak membuatnya PASS.

## 16. Format kasus, bukti, dan laporan

### 16.1 Template kasus terperinci

Gunakan satu record per variasi penting. Tidak perlu menyalin template panjang untuk setiap kombinasi parameter jika dataset dan hasilnya sudah jelas.

```yaml
case_id: KT-18-background-session
source_cases:
  - V-09
  - ARCH-T20
priority: P0
purpose: Membuktikan hasil volume pada konteks background yang dinyatakan sah
preconditions:
  - Aturan dan target volume telah tersimpan
  - Model perangkat, build OS, target SDK, dan TWS dicatat
steps:
  - Mulai sesi melalui alur pengguna yang didukung
  - Pindahkan Krono ke background sesuai prosedur run
  - Buat transisi koneksi TWS yang sah
assertions:
  - Event dan episode yang tepat diterima
  - Guard dan jurnal sah sebelum setter
  - Bukti hasil sesuai klasifikasi
  - Tidak ada setter ganda
status: NOT_RUN
build_id: belum_tersedia
actual_result: belum_dijalankan
evidence: []
```

Contoh ini bukan hasil tes. Pecah P0 untuk guard/pelaporan dan P1 untuk pembuktian kemampuan bila hasil keduanya perlu dilaporkan terpisah.

### 16.2 Informasi wajib pada run perangkat

| Kelompok | Data yang dicatat |
| --- | --- |
| Aplikasi | Version name/code, commit/build ID, varian, target SDK, parameter semantik. |
| Perangkat | Produsen/model, Android/API, build OS, patch sistem, konfigurasi vendor relevan. |
| Bluetooth | Unit TWS, firmware jika diketahui, transport/profil, perangkat lain, rute aktif. |
| Lokasi | Radius/area fixture, akses presisi/background, provider/layanan, kualitas dan waktu pengamatan. |
| Lifecycle | UI terlihat/tidak, proses, sesi/FGS dan jenisnya, asal mulai sesi, layar terkunci. |
| Daya/jaringan | Doze/Battery Saver, charger, jaringan, serta perubahan selama run. |
| Waktu | UTC untuk urutan lintas bukti, waktu monoton dan epoch untuk deadline internal. |
| Hasil | Expected, actual, status tes, hasil aplikasi, jumlah setter, serta alasan bila tidak pasti. |

Data sensitif pada laporan yang dibagikan diganti dengan token. Jangan menghilangkan informasi teknis yang diperlukan untuk mengulang kasus.

### 16.3 Contoh prosedur alur inti lengkap

**Wallpaper A/B:**

1. Buat FX-01 melalui UI dan pastikan file siap.
2. Simpan aktif setelah penjelasan initial check.
3. Jalankan transisi valid A → B dalam pengujian terkontrol.
4. Cocokkan cabang, target, panggilan setter, hasil, dan riwayat.
5. Jalankan rute fisik terpisah untuk memeriksa geofence asli.
6. Ubah wallpaper manual, lalu buka ulang Krono tanpa pemicu baru.
7. Pastikan perubahan manual tidak langsung ditimpa.

**Volume TWS:**

1. Pilih TWS A dan target media 20% melalui UI.
2. Uji perangkat B sebagai kontrol negatif.
3. Jalankan koneksi A dengan konteks audio yang dicatat.
4. Cocokkan episode, guard, journal, setter, serta index hasil.
5. Kirim laporan profil ganda dan worker ulang.
6. Ubah volume manual setelah selesai.
7. Ulangi background pada perangkat/API yang akan diklaim didukung.

Jumlah pengulangan dan variasi dicatat pada run. Satu keberhasilan tidak menghapus kegagalan yang belum dijelaskan.

### 16.4 Template laporan hasil

| Field | Nilai awal |
| --- | --- |
| Kandidat build | Belum tersedia |
| Tanggal/penanggung jawab run | Belum ada run |
| Lingkungan | Belum ditetapkan per run |
| Jumlah PASS / FAIL / BLOCKED / NOT_RUN / NOT_APPLICABLE | Belum dihitung terhadap suite implementasi |
| Kasus P0/P1 belum lulus | Belum dinilai |
| Kemampuan Android terbukti | Belum ada bukti perangkat dari tahap ini |
| Bug penghalang | Belum ada hasil pengujian aplikasi untuk dinilai |
| Keputusan kandidat | Belum dapat dinyatakan layak rilis |

Jangan mengisi “0 bug” sebagai padanan “belum diuji”. Ringkasan menyertakan daftar celah, bukan hanya persentase hijau.

### 16.5 Siklus penanganan bug

Catat langkah reproduksi, fixture, expected/actual, lingkungan, dan bukti. Setelah diperbaiki:

1. Jalankan kasus yang membuktikan kegagalan sebelumnya.
2. Tambahkan regresi pada lapisan paling kecil yang dapat menangkapnya secara bermakna.
3. Jalankan pemeriksaan batas yang ikut berubah.
4. Perbarui hasil terhadap build baru, sambil mempertahankan bukti build lama.

Jangan menambah tes yang hanya mencerminkan baris implementasi atau mengulang semua tes perangkat ketika perubahan tidak menyentuhnya.

## 17. Gerbang kelulusan dan keputusan rilis

### 17.1 Gate usulan

| Gate | Syarat bukti | Menghalangi bila |
| --- | --- | --- |
| G-01: build | Toolchain terkunci, build dapat diulang, graph runtime tunggal, hook tes terpisah. | Build atau identitas dependensi belum jelas. |
| G-02: logika | Kasus P0 core yang berlaku lulus, termasuk UNKNOWN, dedup, konflik, guard, dan deadline. | Ada efek salah/ganda atau hasil yang belum dapat dibuktikan. |
| G-03: data | Constraint, atomicity, crash/reopen, history, aset, dan migrasi yang berlaku lulus. | Kontrol hilang, efek lama terulang, atau konfigurasi rusak. |
| G-04: kemampuan | Bukti fisik mendukung klaim lokasi/TWS/volume/wallpaper per kombinasi yang diumumkan. | Fitur inti hanya berhasil pada fake atau foreground test. |
| G-05: pengalaman | AC produk serta alur UI/aksesibilitas P1 yang berlaku lulus. | Pengguna salah memahami aksi atau tidak dapat menyelesaikan alur utama. |
| G-06: kualitas | Baseline performa tercatat; batas sumber daya ditetapkan; tidak ada Critical/High terbuka. | Klaim performa tanpa pengukuran atau regresi berat belum diselesaikan. |
| G-07: pelaporan | Hasil, celah, batas dukungan, dan bug sesuai kandidat build yang sama. | PASS build lama dipakai untuk perubahan yang belum diperiksa. |

Fitur Should yang disertakan juga harus memenuhi kontraknya. Bila belum disertakan, tulis sebagai belum masuk build, bukan PASS.

Migrasi dari versi produksi lama dapat NOT_APPLICABLE untuk rilis pertama yang belum mempunyai versi sebelumnya. Pembuatan schema, persistensi, reopen, serta recovery versi pertama tetap wajib.

### 17.2 Keputusan ketika kemampuan platform tidak terbukti

Jika jalur audio Android 17 gagal, catat bagian yang gagal: menerima event, memulai layanan yang sah, mempertahankan konteks WIU, meminta volume, atau memverifikasi hasil.

Pilihan pengembangan dapat berupa memperbaiki jalur yang sah, menunda dukungan kombinasi tertentu, atau mengubah ruang dukungan produk secara eksplisit. Perubahan ruang dukungan harus tercermin pada PRD, Capabilities, desain, dan kriteria rilis.

UI yang menjelaskan “perlu membuka aplikasi” dapat lulus tes penjelasan hambatan. Itu tetap tidak meluluskan janji otomasi background yang belum bekerja.

### 17.3 Status saat dokumen ini dibuat

Seluruh KT-01 sampai KT-27 dan seluruh kasus sumber pada bagian 18 berstatus **NOT_RUN** terhadap aplikasi. Tidak ada perangkat, TWS, versi Android, hasil benchmark, atau kemampuan background yang dinyatakan telah lulus melalui dokumen ini.

## 18. Traceability kebutuhan dan kasus sumber

### 18.1 Kebutuhan fungsional dan kualitas

Pemetaan berikut memberi jalur dari kebutuhan ke tes. Kasus penerimaan yang lebih spesifik tetap dijalankan sesuai tabel selanjutnya.

| Kebutuhan PRD | Rute utama | Pemeriksaan |
| --- | --- | --- |
| FR-01, FR-02 | KT-03, KT-07, KT-09, KT-20, KT-21, KT-23 | CRUD, status aktif, persistensi, dan pemulihan. |
| FR-03, FR-04, FR-05 | KT-01, KT-21, KT-24 | Editor sederhana, urutan cabang, dan first match yang dapat diakses. |
| FR-06, FR-07 | KT-02, KT-15, KT-16, KT-17 | Identitas sumber, episode, dan pemantauan lokasi/TWS. |
| FR-08, FR-09, FR-10 | KT-06, KT-13, KT-18, KT-19 | Aset, capability, setter, urutan, serta hasil. |
| FR-11, FR-12, FR-13 | KT-01, KT-08, KT-15, KT-21 | Validasi, kesiapan, dan UNKNOWN. |
| FR-14, FR-15 | KT-03, KT-22 | Simulasi dibanding uji aksi nyata. |
| FR-16, FR-21 | KT-12, KT-19, KT-21 | Hasil per aksi, snapshot, dan hapus history. |
| FR-17, FR-18 | KT-02, KT-03, KT-04, KT-05, KT-11 | Deduplikasi, prioritas, claim, dan worker. |
| FR-19, FR-20 | KT-07, KT-10, KT-15, KT-20 | Perubahan manual dan pemulihan yang konservatif. |
| FR-22 | KT-21, KT-22, KT-24 | Pengaturan, bantuan akses, dan penjelasan tindakan. |
| FR-23, FR-24 | KT-03, KT-12, KT-21, KT-23 | Jika disertakan: contoh/salinan tetap dapat diedit dan tidak otomatis aktif; filter/pencarian tidak mengubah kontrol. |
| FR-25 | KT-12, KT-21, KT-26 | Retensi yang jelas serta batas history. |
| FR-26 | KT-15, KT-16, KT-21 | Jika dwell diimplementasikan: parameter, transisi, dukungan API, serta penjelasan delay diuji; selain itu belum masuk build. |
| NFR-01 | KT-21, KT-22 | Pengguna memahami pemicu, cabang, serta efek nyata melalui sesi keterpahaman. |
| NFR-02 | KT-24, KT-25 | Semantics, font, sentuhan, kontras, dan layout. |
| NFR-03, NFR-04 | KT-13, KT-27 | Data lokal, akses, serta pemisahan ketergantungan platform dari backend. |
| NFR-05 | KT-15, KT-20, KT-26 | Pemantauan terbatas dan pengukuran daya. |
| NFR-06, NFR-07 | KT-01, KT-02, KT-05, KT-09, KT-10, KT-19 | Determinisme, ketertelusuran, serta gangguan tanpa sukses palsu. |
| NFR-08 | KT-06, KT-21, KT-23, KT-26 | UI responsif, state jelas, ingress tidak tertutup setter. |
| NFR-09, NFR-10 | KT-15 sampai KT-20, KT-26 | Matriks dukungan dan batas waktu yang dibuktikan. |

### Cara membaca rentang kasus

“RE-T01 sampai RE-T07” mencakup setiap ID di dalam rentang, bukan satu tes pengganti. Implementasi boleh memakai parameterized test, tetapi laporan tetap menunjukkan variasi dan ID asal yang diperiksa.

Rute KT dapat memakai lebih dari satu lapisan. Prioritas P0/P1 berarti aspek kontrol diperiksa sebagai P0 dan kemampuan/alur sebagai P1 sesuai resep. Eksperimen perangkat tetap memerlukan bukti fisik meskipun logikanya sudah lulus dengan fake.

Seluruh baris berikut berstatus **NOT_RUN**. Hasil yang diharapkan mengikuti dokumen asal dan assertion pada rute KT.

### 18.2 Kriteria penerimaan produk

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| AC-01 | Persistensi aturan, identitas pilihan, dan command simpan. | KT-03, KT-09, KT-21, KT-23 | P0/P1 |
| AC-02 | TWS pilihan dibanding perangkat lain; kemampuan dan hasil volume. | KT-17, KT-18, KT-19 | P0/P1 |
| AC-03 | Perubahan A/B sampai pemasangan wallpaper yang sesuai. | KT-16, KT-19 | P0/P1 |
| AC-04 sampai AC-05 | First match, UNKNOWN, dan hambatan akses. | KT-01, KT-15, KT-22 | P0/P1 |
| AC-06 | Perubahan manual tidak dipaksa kembali. | KT-07 | P0 |
| AC-07 sampai AC-08 | Kejadian ganda dan prioritas batch. | KT-02, KT-03, KT-04, KT-11 | P0 |
| AC-09 | Urutan aksi dan hasil sebagian berhasil. | KT-07, KT-19 | P0 |
| AC-10 | Simulasi berbeda dari uji nyata. | KT-22 | P0 |
| AC-11 | Jeda menghentikan langkah baru tanpa rollback. | KT-07, KT-21 | P0 |
| AC-12 | Reboot dan pemulihan pemantauan tanpa replay. | KT-10, KT-15, KT-20 | P0/P1 |
| AC-13 | Hapus history mempertahankan aturan. | KT-12, KT-21 | P0/P1 |

### 18.3 Eksperimen Android Capabilities

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| V-01 | Bluetooth tanpa memaksa lokasi. | KT-15, KT-17 | P1 |
| V-02 sampai V-05 | Penerimaan saat proses mati, identitas, profil ganda, dan LE Audio. | KT-02, KT-17 | P0/P1 |
| V-06 | Volume saat UI terlihat dan pemetaan index. | KT-19 | P1 |
| V-07 sampai V-10 | Audio Android 17, WIU, serta pemulihan sesi. | KT-18, KT-20 | P0/P1 |
| V-11 sampai V-13 | Dua perangkat, no change, serta perubahan manual. | KT-07, KT-19 | P0/P1 |
| V-14 sampai V-15 | Precise/approximate dan background location. | KT-15 | P1 |
| V-16 sampai V-17 | A/B, overlap, serta UNKNOWN. | KT-01, KT-16 | P0/P1 |
| V-18 sampai V-20 | Kapasitas, layanan hilang, boot/update, dan Force Stop. | KT-15, KT-20 | P0/P1 |
| V-21 sampai V-23 | Target wallpaper, akses gambar setelah restart, dan sumber hilang. | KT-13, KT-19 | P0/P1 |
| V-24 sampai V-25 | Doze/vendor serta penolakan notifikasi. | KT-18, KT-20 | P0/P1 |

### 18.4 Kasus Rule Engine

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| RE-T01 sampai RE-T07 | Seluruh variasi cabang TRUE/FALSE/UNKNOWN. | KT-01 | P0 |
| RE-T08 sampai RE-T12 | Sumber terkait, profil, episode, dan laporan lama. | KT-02, KT-16, KT-17 | P0 |
| RE-T13 sampai RE-T14 | Command aktivasi serta pertemuan initial check/event. | KT-02, KT-03 | P0 |
| RE-T15 sampai RE-T16 | Deadline tepat batas dan akses pulih terlambat. | KT-08 | P0 |
| RE-T17 sampai RE-T20 | Arbitrase, konflik seluruh cabang, dan tidak ada promosi. | KT-04 | P0 |
| RE-T21 sampai RE-T24 | Target lebih baru serta perubahan sumber/kontrol/prioritas. | KT-05, KT-06, KT-07 | P0 |
| RE-T25 sampai RE-T27 | Silent setter, no change, dan perubahan manual. | KT-07, KT-19 | P0 |
| RE-T28 sampai RE-T29 | Crash setelah REQUESTING dan worker ulang. | KT-10, KT-11 | P0 |
| RE-T30 sampai RE-T31 | Agregat partial dan UNVERIFIED. | KT-19 | P0 |
| RE-T32 sampai RE-T34 | Simulasi, dedup uji aksi, dan konfigurasi di luar MVP. | KT-01, KT-03, KT-22 | P0 |
| RE-T35 sampai RE-T37 | Jam, pembukaan UI, dan kesempatan CLOSED. | KT-08, KT-11, KT-23 | P0 |
| RE-T38 sampai RE-T40 | Episode lintas cause, kelanjutan valid, dan pihak kalah. | KT-02, KT-04, KT-08 | P0 |

### 18.5 Kasus Architecture

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| ARCH-T01 | Core JVM tanpa Android dan evaluator yang dapat diuji. | KT-01, KT-05 | P0 |
| ARCH-T02 sampai ARCH-T04 | Flow, command ganda, serta initial check bertemu event. | KT-02, KT-03, KT-23 | P0 |
| ARCH-T05 sampai ARCH-T07 | Klaim bersamaan, revisi saat commit, dan reorder. | KT-05, KT-07, KT-09 | P0 |
| ARCH-T08 sampai ARCH-T09 | Preflight dan konflik seluruh cabang. | KT-04 | P0 |
| ARCH-T10 sampai ARCH-T11 | Ingress saat setter sibuk dan jeda antaraksi. | KT-06, KT-07 | P0 |
| ARCH-T12 sampai ARCH-T16 | Crash, deadline, CLOSED, dan round kelanjutan. | KT-08, KT-10 | P0 |
| ARCH-T17 sampai ARCH-T18 | Cakupan geofence serta normalisasi profil TWS. | KT-02, KT-16, KT-17 | P0 |
| ARCH-T19 sampai ARCH-T20 | Perubahan manual serta silent setter Android 17. | KT-07, KT-18, KT-19 | P0 |
| ARCH-T21 sampai ARCH-T22 | Aset hilang/impor gagal dan penghapusan history. | KT-12, KT-13 | P0/P1 |
| ARCH-T23 sampai ARCH-T24 | Simulasi dan commit REQUESTING gagal. | KT-09, KT-22 | P0 |
| ARCH-T25 sampai ARCH-T26 | Boot tanpa mewarisi sesi audio serta izin notifikasi. | KT-18, KT-20 | P0/P1 |

### 18.6 Kasus Database

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| DB-T01 sampai DB-T02 | Resi command sama dan hash berbeda. | KT-03, KT-09 | P0 |
| DB-T03 sampai DB-T05 | Revisi atomik, FK sumber, dan draf persisten. | KT-09, KT-21, KT-23 | P0/P1 |
| DB-T06 sampai DB-T08 | Event/episode ganda serta unique ACTION_TEST dengan NULL. | KT-02, KT-03, KT-09 | P0 |
| DB-T09 sampai DB-T14 | CAS, deadline, seluruh target, generasi, dan sumber baru. | KT-05, KT-06, KT-08, KT-09 | P0 |
| DB-T15 sampai DB-T16 | Recovery REQUESTING dibanding PREPARED/menunggu. | KT-10 | P0 |
| DB-T17 sampai DB-T18 | Pihak kalah tidak dipromosikan dan posisi prioritas unik. | KT-04, KT-07, KT-09 | P0 |
| DB-T19 sampai DB-T21 | Watermark, retensi, dan snapshot aturan terhapus. | KT-12 | P0 |
| DB-T22 sampai DB-T23 | Aset bersama serta race lease/GC. | KT-13 | P0/P1 |
| DB-T24 sampai DB-T25 | Migrasi kontrol dan worker setelah compaction. | KT-11, KT-12, KT-14 | P0 |
| DB-T26 sampai DB-T28 | Agregat hasil, commit gagal, serta query tanpa efek. | KT-09, KT-12, KT-19, KT-21 | P0/P1 |

### 18.7 Kasus Design

| ID asal | Fokus yang dipertahankan | Rute tes | Prioritas |
| --- | --- | --- | --- |
| DES-T01 sampai DES-T03 | Pengguna baru, aktif terhalang, dan draf. | KT-21, KT-23 | P0/P1 |
| DES-T04 sampai DES-T07 | Area A/B, urutan, UNKNOWN, dan aksi terhalang. | KT-01, KT-16, KT-21 | P0/P1 |
| DES-T08 sampai DES-T10 | Sumber bersama, nama TWS sama, dan impor cloud gagal. | KT-13, KT-17, KT-21 | P1 |
| DES-T11 sampai DES-T14 | Draf tanpa setter, urutan, initial check, dan ketuk ganda. | KT-03, KT-07, KT-21, KT-23 | P0/P1 |
| DES-T15 sampai DES-T16 | Batas simulasi serta peringatan uji nyata. | KT-22 | P0 |
| DES-T17 sampai DES-T18 | Audio Android 17 dan penolakan notifikasi. | KT-18, KT-20, KT-21 | P0/P1 |
| DES-T19 sampai DES-T20 | Partial dan hasil belum pasti tanpa retry UI. | KT-10, KT-19, KT-21 | P0 |
| DES-T21 sampai DES-T23 | Prioritas global, hapus history, dan aturan terhapus. | KT-07, KT-12, KT-21 | P0/P1 |
| DES-T24 sampai DES-T25 | Font besar, IME, TalkBack, dan reorder. | KT-24, KT-25 | P1 |
| DES-T26 sampai DES-T28 | Refresh/rotasi, state visual, serta perubahan jendela. | KT-23, KT-24, KT-25 | P0/P1 |

### 18.8 Rekap cakupan rencana

| Keluarga kasus | Jumlah ID sumber | Status aplikasi |
| --- | --- | --- |
| AC | 13 | NOT_RUN |
| V | 25 | NOT_RUN |
| RE-T | 40 | NOT_RUN |
| ARCH-T | 26 | NOT_RUN |
| DB-T | 28 | NOT_RUN |
| DES-T | 28 | NOT_RUN |
| **Total** | **160** | **Belum ada hasil pengujian aplikasi** |

FR-01 sampai FR-26 dan NFR-01 sampai NFR-10 adalah kebutuhan yang ditautkan, bukan tambahan angka PASS. Banyaknya ID tidak menunjukkan jumlah metode tes atau tingkat coverage implementasi.

## 19. Keputusan terbuka sebelum implementasi tes

| ID | Keputusan yang diperlukan | Cara menutup |
| --- | --- | --- |
| TEST-OPEN-01 | Kombinasi Kotlin, KSP, Hilt, runner, dan artefak tes. | Bootstrap build nyata, kunci versi kompatibel, simpan hasil build dan suite awal. |
| TEST-OPEN-02 | Model HP, OS, vendor, serta TWS yang tersedia. | Isi inventaris dan matriks dukungan; jangan mengganti fisik dengan fake tanpa mencatat batasnya. |
| TEST-OPEN-03 | Jalur sesi audio Android 17 yang sah. | Jalankan KT-18 dan pisahkan hasil kemampuan dari penanganan hambatan. |
| TEST-OPEN-04 | Provider peta serta prosedur rute geofence. | Tentukan fixture lapangan, radius, kualitas lokasi, dan format pencatatan delay. |
| TEST-OPEN-05 | Batas ukuran gambar, anggaran waktu, memori, dan baterai. | Ukur baseline KT-13/KT-26 lalu tetapkan batas produk serta metrik. |
| TEST-OPEN-06 | Alat screenshot yang cocok dengan baseline proyek. | Buktikan kompatibilitas; tetapkan lingkungan render dan proses review acuan. |
| TEST-OPEN-07 | Fitur Should yang masuk kandidat pertama. | Tentukan contoh/duplikasi/filter/retensi/dwell yang benar-benar dibangun dan kasus yang berlaku. |
| TEST-OPEN-08 | Schema produksi pertama dan dukungan upgrade. | Ekspor schema nyata, simpan fixture, lalu tambahkan migrasi berdasarkan versi yang benar-benar dirilis. |
| TEST-OPEN-09 | Sesi uji keterpahaman pengguna. | Tentukan peserta/prosedur, catat temuan, dan perbaiki istilah atau alur yang menyesatkan. |

Keputusan terbuka bukan alasan menghentikan seluruh pekerjaan. Tes core serta rancangan fixture dapat dibuat lebih dahulu. Klaim yang bergantung pada perangkat, kemampuan platform, atau anggaran numerik menunggu bukti terkait.

## 20. Dasar dan referensi resmi

### 20.1 Dasar proyek

Dokumen ini menggunakan PRD, Android Capabilities, Rule Engine, Architecture, Database, dan Design Krono v0.1 yang disusun dalam rangkaian ini.

Baseline teknis, parameter waktu, hasil eksekusi, serta kebijakan penyimpanan mengikuti dokumen tersebut. Rute KT, pengelompokan prioritas, dataset, urutan pengujian, dan gate rilis pada TESTING.md merupakan usulan pengujian v0.1.

### 20.2 Referensi eksternal

Diperiksa pada **22 Oktober 2026**. Gunakan versi/tag yang benar-benar dikunci proyek saat mengadopsi kode atau konfigurasi. Referensi berikut menjelaskan API dan metode; tidak satu pun merupakan hasil tes aplikasi Krono.

| ID | Referensi | Pemakaian |
| --- | --- | --- |
| S01 | [Android testing strategies](https://developer.android.com/training/testing/fundamentals/strategies) | Ukuran tes dan kemiripan lingkungan dengan produksi. |
| S02 | [What to test in Android](https://developer.android.com/training/testing/fundamentals/what-to-test) | Pemilihan pemeriksaan bermakna serta batas coverage. |
| S03 | [Use test doubles in Android](https://developer.android.com/training/testing/fundamentals/test-doubles) | Pemisahan komponen asli dan pengganti tes. |
| S04 | [Room 3 release notes](https://developer.android.com/jetpack/androidx/releases/room3) | Baseline Room 3.0.3 dan schema export. |
| S05 | [kotlinx-coroutines-test API](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | runTest, scheduler, dispatcher, dan waktu virtual. |
| S06 | [Room 3 MigrationTestHelper](https://developer.android.com/reference/androidx/room3/testing/MigrationTestHelper) | Validasi pembuatan database dan migrasi dengan namespace Room 3. |
| S07 | [Integration tests with WorkManager](https://developer.android.com/develop/background-work/background-tasks/testing/persistent/integration-testing) | Harness WorkManager dan pengiriman pekerjaan terkontrol. |
| S08 | [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing) | Interaksi dan assertion UI berbasis semantics. |
| S09 | [Hilt testing guide](https://developer.android.com/training/dependency-injection/hilt-testing) | Penggantian dependensi serta graph tes. |
| S10 | [Build-managed devices](https://developer.android.com/studio/test/managed-devices) | Lingkungan instrumentasi yang dapat dikonfigurasi serta diulang. |
| S11 | [Write a Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) | Pengukuran alur aplikasi pada build benchmark yang sesuai. |
| S12 | [Compose Preview Screenshot Testing](https://developer.android.com/studio/preview/compose-screenshot-testing) | Status experimental dan perubahan jalur konfigurasi screenshot. |
| S13 | [Android 17 background audio hardening](https://developer.android.com/about/versions/17/changes/bg-audio) | Lifecycle audio, WIU, silent failure, dan diagnostik. |
| S14 | [Create and monitor geofences](https://developer.android.com/develop/sensors-and-location/location/geofencing) | Kapasitas registrasi, kualitas, dan kemungkinan keterlambatan. |
| S15 | [Processes and app lifecycle](https://developer.android.com/guide/components/activities/process-lifecycle) | Pemisahan lifecycle proses dari asumsi UI. |
| S16 | [Compose accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing) | Pengujian semantics, otomatis, dan layanan aksesibilitas manual. |
| S17 | [Synchronize Compose tests](https://developer.android.com/develop/ui/compose/testing/synchronization) | Sinkronisasi serta batas pengendalian clock UI. |
| S18 | [android/nowinandroid](https://github.com/android/nowinandroid) | Acuan repository aplikasi Compose dan pengujian. |
| S19 | [android/platform-samples](https://github.com/android/platform-samples) | Acuan contoh API platform. |
| S20 | [androidx/androidx](https://github.com/androidx/androidx) | Sumber resmi AndroidX untuk penelusuran API dan tes. |
| S21 | [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Sumber resmi coroutine serta test scheduler. |
| S22 | [Back up user data with Auto Backup](https://developer.android.com/identity/data/autobackup) | Konfigurasi backup serta batas allowBackup pada transfer perangkat tertentu. |

## 21. Riwayat dokumen dan tahap berikutnya

| Versi | Tanggal | Perubahan |
| --- | --- | --- |
| 0.1 | 22 Oktober 2026 | Strategi 6 lapisan, 27 rute tes, pemetaan 160 kasus sumber, matriks perangkat, fault injection, UI/aksesibilitas, format bukti, dan gate rilis. |

Dokumen nomor 7 selesai sebagai rencana. Pelaksanaan pengujian menunggu implementasi dan lingkungan yang sesuai. Tahap nomor 8 adalah IMPLEMENTATION_PLAN.md, yang membagi pembangunan Krono menjadi langkah kerja berdasarkan kontrak serta gerbang pembuktian di atas.






