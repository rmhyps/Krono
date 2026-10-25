# Krono

## Product Requirements Document

| Atribut | Keterangan |
| --- | --- |
| Nama produk | Krono |
| Nama dokumen | `PRD.md` |
| Versi dokumen | 0.1 |
| Tanggal penyusunan | 22 Oktober 2026, Asia/Jakarta |
| Bahasa | Bahasa Indonesia |
| Tahap | Rancangan kebutuhan produk sebelum implementasi |
| Status | Kebutuhan inti terkonfirmasi; rincian MVP merupakan usulan v0.1 |
| Platform kerja | Android sebagai usulan awal, berdasarkan referensi desain yang diberikan |

> Dokumen ini menjelaskan kebutuhan produk dan perilaku yang diharapkan. Dokumen ini belum membuktikan bahwa aplikasi telah dibuat, diuji, atau mendukung seluruh perangkat Android. Rincian implementasi dan batasan setiap API akan ditetapkan pada dokumen lanjutan.

## 1. Tujuan dokumen

PRD ini menjadi acuan untuk memahami masalah yang diselesaikan Krono, siapa penggunanya, apa yang perlu tersedia pada versi pertama, dan bagaimana keberhasilan fiturnya diperiksa.

Dokumen ini mencakup kebutuhan pengguna, batas produk, alur penggunaan, kebutuhan fungsional, kebutuhan kualitas, dan kriteria penerimaan. Struktur kode, skema tabel, algoritma penjadwalan, dan spesifikasi visual terperinci menjadi tanggung jawab dokumen berikutnya.

### 1.1 Cara membaca status keputusan

| Penanda | Makna |
| --- | --- |
| Terkonfirmasi | Dinyatakan langsung oleh pemilik produk dalam pembahasan Krono. |
| Usulan v0.1 | Pilihan rancangan yang diajukan agar spesifikasi dapat digunakan sebagai dasar kerja. Pilihan ini belum dianggap sebagai keputusan final pemilik produk. |
| Perlu verifikasi | Kemampuan atau perilaku yang masih harus diperiksa melalui dokumentasi, purwarupa, atau pengujian perangkat. |

Prioritas **Must**, **Should**, dan **Later** menyatakan urutan kebutuhan dalam usulan MVP. Prioritas tersebut tidak mengubah status persetujuan keputusan. Seluruh rincian perilaku dalam dokumen ini berstatus **Usulan v0.1**, kecuali yang secara eksplisit ditandai Terkonfirmasi.

## 2. Ringkasan produk

Krono adalah aplikasi otomasi pribadi pada HP. Pengguna menyusun aturan melalui pilihan visual, lalu Krono memeriksa kondisi dan menjalankan aksi ketika terjadi kejadian yang relevan.

Contoh kebutuhan inti adalah mengganti wallpaper ketika pengguna berada di area tertentu dan mengecilkan volume ketika HP terhubung ke TWS pilihan pengguna. Satu aturan dapat memiliki beberapa cabang IF, ELSE IF, dan ELSE sesuai kebutuhan.

Pengguna memilih pemicu, kondisi, dan aksi tanpa harus menulis kode. Krono menyediakan informasi kesiapan aturan dan riwayat eksekusi agar pengguna dapat memahami apa yang terjadi.

**Pernyataan produk:** Krono membantu pengguna membuat perilaku HP menyesuaikan situasi sehari-hari melalui aturan yang mudah disusun dan bisa diperiksa hasilnya.

## 3. Dasar kebutuhan dan status keputusan

### 3.1 Kebutuhan yang terkonfirmasi

| ID | Kebutuhan | Dasar |
| --- | --- | --- |
| K-01 | Nama aplikasi adalah Krono. | Permintaan langsung pemilik produk. |
| K-02 | Fungsi utama berupa otomasi atau shortcut dengan logika kondisi dan aksi. | Penjelasan konsep produk. |
| K-03 | Mendukung contoh lokasi A dengan wallpaper A dan lokasi B dengan wallpaper B. | Contoh penggunaan utama. |
| K-04 | Mendukung contoh koneksi TWS tertentu yang diikuti pengaturan volume menjadi lebih kecil. | Contoh penggunaan utama. |
| K-05 | Lampiran desain dan tangkapan layar menjadi referensi awal tampilan. | Lampiran yang diberikan; desain akhir masih terbuka. |
| K-06 | Rancangan teknis menggunakan referensi yang mutakhir. | Permintaan langsung pemilik produk. |
| K-07 | Dokumen disusun bertahap, dimulai dari PRD. | Urutan pekerjaan yang diminta. |

### 3.2 Usulan dasar untuk versi pertama

| ID | Usulan v0.1 | Alasan |
| --- | --- | --- |
| U-01 | Memulai dari Android. | Referensi desain menyebut Android dan kebutuhan inti berhubungan dengan pengaturan perangkat. |
| U-02 | Menjalankan aturan dan menyimpan datanya di HP. | Dua contoh inti dapat dirancang tanpa akun atau server aplikasi. Ketergantungan layanan lokasi tetap diperiksa terpisah. |
| U-03 | Memprioritaskan lokasi, Bluetooth, wallpaper, dan volume media. | Mencakup contoh inti dengan ruang lingkup yang dapat diuji secara bertahap. |
| U-04 | Memakai editor berbentuk urutan vertikal dengan percabangan opsional. | Mendukung layar HP dan aturan sederhana maupun bercabang. |
| U-05 | Menyediakan tab Aturan dan Riwayat, dengan pengaturan melalui tombol terpisah. | Mengikuti susunan utama pada referensi tampilan. |
| U-06 | Memulai dari akses Android standar yang diberikan pengguna. | Dukungan akses istimewa belum menjadi kebutuhan inti. |
| U-07 | Menjadikan Kotlin dan Jetpack Compose sebagai kandidat teknologi. | Sesuai arah rekomendasi Android; keputusan akhir berada di `ARCHITECTURE.md`. [R1][R2] |

Versi Android minimum, target SDK, model distribusi aplikasi, serta daftar perangkat yang didukung belum ditetapkan. Angka versi dan library tidak dikunci dalam PRD ini.

## 4. Masalah yang diselesaikan

Pengguna sering mengulang perubahan pengaturan saat konteksnya berubah. Contohnya mengganti wallpaper sesuai lokasi atau mengecilkan volume setelah TWS terhubung. Pengaturan yang dilakukan manual bisa terlupa dan harus diulang pada situasi yang sama.

Penyusunan otomasi juga dapat membingungkan apabila pengguna langsung berhadapan dengan istilah teknis, terlalu banyak pilihan, atau perilaku IF/ELSE yang tidak dijelaskan. Setelah aturan dibuat, pengguna perlu mengetahui apakah aturan siap digunakan dan mengapa sebuah aksi gagal.

Krono perlu menyelesaikan tiga kebutuhan sekaligus: memudahkan pembuatan aturan, menjalankannya sesuai logika yang jelas, dan menjelaskan hasilnya melalui tampilan yang mudah dipahami.

## 5. Pengguna yang dituju

### 5.1 Pengguna utama

Pemilik HP yang ingin menyesuaikan perilaku perangkat dengan lokasi atau perangkat Bluetooth yang digunakan, tanpa menulis program.

Kebutuhan utama mereka adalah membuat aturan sederhana dengan cepat, mengetahui izin yang diperlukan, dan memahami hasil eksekusi melalui bahasa sehari-hari.

### 5.2 Pengguna lanjutan

Pengguna yang ingin menambahkan beberapa cabang kondisi atau beberapa aksi dalam satu aturan. Mereka membutuhkan urutan eksekusi yang jelas, prioritas ketika aturan bertabrakan, dan alat untuk memeriksa aturan sebelum digunakan.

Nama persona, usia, pekerjaan, ukuran pasar, dan hasil riset pengguna belum ditentukan. Profil di atas merupakan sasaran rancangan, bukan temuan riset lapangan.

## 6. Tujuan dan indikator keberhasilan

| Tujuan | Indikator yang dapat diperiksa |
| --- | --- |
| Pengguna dapat menyusun aturan tanpa menulis kode. | Alur lokasi dan TWS dapat dikonfigurasi melalui formulir dan pilihan visual. |
| Hasil eksekusi sesuai kondisi yang dipilih. | Skenario penerimaan pada bagian 16 menghasilkan cabang dan aksi yang sesuai. |
| Pengguna memahami kesiapan aturan. | Aturan yang memerlukan izin, dijeda, atau menunggu pemicu memiliki label berbeda. |
| Pengguna dapat memahami kegagalan. | Riwayat menunjukkan aksi yang gagal, penyebab yang diketahui, dan langkah perbaikan yang relevan. |
| Pengguna tetap mengendalikan HP. | Aturan dapat dijeda; perubahan manual tidak terus ditimpa tanpa pemicu baru. |
| Produk memiliki batas dukungan yang jujur. | Aksi yang belum didukung tidak ditampilkan sebagai fitur siap digunakan. |

Indikator dinilai melalui skenario penggunaan dan pengujian. Target numerik performa, konsumsi baterai, dan waktu penyelesaian tugas akan ditetapkan setelah pengukuran awal. PRD ini tidak mengklaim hasil pengujian atau tingkat keberhasilan yang belum diukur.

## 7. Istilah produk

| Istilah | Arti dalam Krono |
| --- | --- |
| Aturan | Satu konfigurasi otomasi yang memiliki nama, pemicu, kondisi opsional, dan aksi. |
| Pemicu / WHEN | Kejadian yang membuat Krono perlu memeriksa sebuah aturan. |
| Kondisi / IF | Pemeriksaan terhadap keadaan yang relevan pada saat aturan dievaluasi. |
| ELSE IF | Kondisi berikutnya yang diperiksa apabila kondisi sebelumnya tidak cocok. |
| ELSE | Aksi cadangan ketika seluruh kondisi sebelumnya diketahui tidak cocok. |
| Aksi / THEN | Perubahan yang diminta pengguna, misalnya memasang wallpaper atau mengatur volume media. |
| Cabang | Satu kondisi beserta daftar aksi yang dipilih jika kondisi tersebut cocok. |
| Area lokasi | Wilayah yang ditentukan oleh titik pusat dan radius. |
| Eksekusi | Satu upaya menjalankan aturan beserta hasil masing-masing aksinya. |
| Kesiapan | Keadaan yang menunjukkan apakah konfigurasi, akses, dan sumber daya aturan tersedia. |
| Simulasi | Pemeriksaan logika menggunakan masukan contoh, tanpa mengubah pengaturan HP. |

Istilah yang ditampilkan kepada pengguna diutamakan dalam bahasa sederhana, seperti Ketika, Dengan syarat, Lakukan, dan Kalau tidak. Istilah IF/ELSE dapat tetap tersedia sebagai penjelas dalam editor.

## 8. Ruang lingkup versi pertama

### 8.1 Must

1. Membuat, membaca, mengubah, menjeda, mengaktifkan, dan menghapus aturan.
2. Memilih satu keluarga pemicu per aturan: perubahan area lokasi atau perubahan koneksi Bluetooth perangkat terpilih.
3. Memantau area atau perangkat yang digunakan oleh cabang aturan tersebut.
4. Membuat alur sederhana Ketika → Lakukan tanpa mewajibkan kondisi tambahan.
5. Menambahkan cabang IF, ELSE IF, dan ELSE opsional dalam satu tingkat percabangan.
6. Menjalankan satu atau beberapa aksi berurutan pada cabang yang dipilih.
7. Memasang gambar wallpaper pilihan pengguna pada sasaran layar yang didukung perangkat.
8. Mengatur volume media dengan nilai yang dipilih pengguna.
9. Memeriksa kesiapan aturan dan menjelaskan izin yang diperlukan sesuai fitur.
10. Menyediakan simulasi logika dan uji aksi secara terpisah.
11. Menyimpan konfigurasi serta riwayat eksekusi secara lokal.
12. Menangani kejadian ganda, konflik aksi, dan pemulihan pemantauan sesuai batas platform.

### 8.2 Should

- Pilihan awal Wallpaper Lokasi dan Volume TWS yang dapat diedit sebelum diaktifkan.
- Duplikasi aturan untuk membuat variasi dengan cepat.
- Filter daftar aturan menurut jenis pemicu dan filter riwayat menurut hasil.
- Pencarian aturan jika daftar sudah panjang.
- Pengaturan penghapusan dan batas riwayat agar data tidak tumbuh tanpa batas.
- Pengaturan durasi menetap di area untuk kebutuhan yang sesuai dengan dukungan platform.

### 8.3 Later dan di luar MVP usulan

| Kemampuan | Posisi dalam rancangan awal |
| --- | --- |
| Pemicu waktu, Wi-Fi, baterai, notifikasi, dan sensor lain | Kandidat perluasan setelah dua keluarga pemicu awal terbukti bekerja. |
| AND/OR untuk kelompok kondisi dan percabangan bertingkat | Model kebutuhan lanjutan; tidak diwajibkan pada editor MVP. |
| DND | Kandidat lanjutan yang memerlukan pemeriksaan akses dan perilaku Android. |
| Battery Saver dan mode hemat daya khusus vendor | Belum menjadi janji fitur; kemampuan akses harus diverifikasi. |
| Membuka aplikasi atau navigasi otomatis dari background | Kandidat lanjutan dengan batasan peluncuran layar Android. |
| Akun, backend, sinkronisasi cloud, dan berbagi aturan | Belum dibutuhkan untuk dua alur inti. |
| Ekspor dan impor aturan | Kandidat setelah bentuk data aturan stabil. |
| Editor kanvas bebas, marketplace, dan pembuat aturan berbasis AI | Tidak termasuk ruang lingkup MVP usulan. |
| Dukungan iOS dan pengelolaan perangkat lain dari jarak jauh | Belum ditetapkan. |
| Root, Shizuku, dan otomasi melalui Accessibility Service | Tidak menjadi syarat penggunaan versi awal. |

Elemen yang muncul di mockup seperti Cloud Sync, Nav Mode, DND, dan Ultra Low Power merupakan contoh visual, bukan bukti fitur telah didukung. Penempatan fitur pada Later merupakan usulan prioritas, bukan penolakan kebutuhan tambahan dari pemilik produk.

## 9. Skenario penggunaan utama

### 9.1 Wallpaper berdasarkan lokasi

Pengguna membuat aturan Wallpaper Lokasi, menentukan area A dan area B, lalu memilih gambar untuk setiap area.

```text
KETIKA status area yang dipantau berubah
JIKA berada di area A
    LAKUKAN pasang gambar A
KALAU TIDAK, JIKA berada di area B
    LAKUKAN pasang gambar B
KALAU TIDAK
    Pertahankan wallpaper terakhir, kecuali pengguna memilih aksi cadangan
```

Hasil yang diharapkan:

- Area ditentukan dengan titik pusat dan radius, bukan kecocokan pada satu koordinat persis. Geofencing Android memakai model area seperti ini. [R3]
- Perpindahan A ke B tetap relevan meskipun pengguna berada di dalam kawasan yang lebih besar, misalnya kampus yang sama.
- Jika area A dan B tumpang tindih, cabang pertama yang cocok dipilih sesuai urutan yang terlihat di editor.
- Jika data lokasi belum tersedia atau aksesnya terputus, Krono menampilkan bahwa kondisi belum dapat diperiksa. ELSE tidak dijalankan berdasarkan data yang tidak diketahui.
- Keluar dari seluruh area tidak otomatis mengembalikan wallpaper lama. Perilaku awal yang diusulkan adalah mempertahankan wallpaper terakhir; aksi cadangan harus dipilih secara eksplisit.

Peristiwa geofence dapat diterima dengan jeda dari sistem. Produk tidak menjanjikan pergantian wallpaper seketika saat melewati garis radius. [R3]

### 9.2 Volume ketika TWS terhubung

Pengguna membuat aturan Volume TWS, memilih perangkat tertentu, dan menetapkan volume media yang diinginkan, misalnya 20%.

```text
KETIKA TWS pilihan pengguna terhubung
LAKUKAN atur volume media menjadi 20%
```

Hasil yang diharapkan:

- Perangkat dipilih melalui daftar perangkat yang dapat diakses dengan izin yang sesuai.
- Perubahan koneksi perangkat lain tidak memicu aturan ini.
- Jenis volume ditulis sebagai Volume media. Pengaturan nada dering, alarm, atau panggilan tidak ikut diasumsikan berubah.
- Persentase merupakan pilihan produk yang dipetakan ke tingkat volume yang tersedia pada perangkat; dukungan aktual harus diuji. Android menyediakan API pengaturan tingkat volume per stream. [R6]
- Setelah aksi selesai, pengguna boleh mengubah volume secara manual. Krono tidak berulang kali memaksakan nilai lama tanpa pemicu baru.
- Ketika TWS terputus, nilai volume terakhir dipertahankan sebagai perilaku awal usulan. Pengembalian nilai sebelumnya belum menjadi janji MVP.

Koneksi perangkat dan keluaran audio aktif dapat berbeda pada konfigurasi beberapa perangkat. Cakupan dukungannya harus dijelaskan dalam `ANDROID_CAPABILITIES.md` sebelum diiklankan sebagai fitur yang didukung.

### 9.3 Satu aturan dengan beberapa aksi

Sebuah cabang dapat memuat beberapa aksi dari jenis yang didukung. Contohnya, ketika perangkat pilihan terhubung, atur volume media lalu pasang gambar tertentu.

Urutan aksi mengikuti urutan yang terlihat di editor. Jika aksi pertama berhasil dan aksi berikutnya gagal, riwayat menunjukkan hasil Sebagian berhasil beserta rincian setiap aksi. Krono tidak mengklaim seluruh perubahan telah dibatalkan.

### 9.4 Aturan belum siap

Pengguna menyimpan konfigurasi lokasi, tetapi belum memberikan akses lokasi yang diperlukan. Aturan dapat tersimpan sebagai draf. Ketika aktivasi diminta, Krono menjelaskan kebutuhan akses beserta cara melengkapinya.

Jika akses dicabut setelah aturan aktif, keinginan pengguna untuk mengaktifkan aturan tetap disimpan, tetapi kesiapan berubah menjadi Perlu izin. Aksi yang bergantung pada akses tersebut tidak dijalankan dengan asumsi kondisi palsu.

## 10. Alur penggunaan

### 10.1 Membuka Krono untuk pertama kali

1. Pengguna membuka layar Aturan yang masih kosong.
2. Krono menjelaskan fungsi aplikasi secara singkat dan menyediakan Buat aturan.
3. Pengguna dapat memilih contoh awal atau aturan kosong.
4. Izin diminta ketika fitur yang membutuhkannya dipilih, disertai penjelasan yang relevan.
5. Pengguna dapat membatalkan proses atau menyimpan draf jika konfigurasi belum lengkap.

### 10.2 Membuat aturan

1. Isi nama aturan.
2. Pilih kejadian pemicu.
3. Pilih area atau perangkat yang akan dipantau.
4. Pilih aksi dan parameternya.
5. Tambahkan kondisi atau cabang lain apabila diperlukan.
6. Baca ringkasan perilaku aturan, termasuk ketika tidak ada kondisi yang cocok.
7. Jalankan simulasi atau uji aksi jika diinginkan.
8. Simpan dan pilih apakah aturan akan diaktifkan.

Usulan perilaku saat aktivasi pertama adalah memeriksa keadaan terkini satu kali jika data valid tersedia. Editor perlu menjelaskan bahwa aksi yang cocok dapat langsung dijalankan saat diaktifkan. Pemeriksaan awal tersebut harus dibedakan dari peristiwa baru pada riwayat. Jika data belum tersedia, aturan menunggu data yang valid.

### 10.3 Mengubah, menjeda, dan menghapus aturan

- Mengubah parameter tidak langsung menjalankan aksi selama pengguna masih mengedit.
- Saat menyimpan revisi, Krono memvalidasi konfigurasi dan memperbarui pemantauan yang relevan. Evaluasi awal setelah revisi mengikuti penjelasan aktivasi di atas.
- Menjeda aturan menghentikan penjadwalan aksi baru dari aturan tersebut. Perubahan yang sudah diterapkan tidak otomatis dipulihkan.
- Jika aksi sedang berjalan ketika aturan dijeda, aksi yang belum dimulai dibatalkan apabila memungkinkan. Hasil aksi yang sudah berlangsung tetap dicatat.
- Penghapusan aturan menjelaskan dampaknya terhadap pemantauan dan riwayat. Riwayat lama tetap memiliki nama aturan saat eksekusi, kecuali pengguna memilih menghapus riwayat terkait.
- Membuka ulang layar atau menyegarkan daftar tidak dianggap sebagai pemicu untuk menjalankan aturan kembali.

### 10.4 Memeriksa hasil

1. Buka tab Riwayat.
2. Pilih satu eksekusi.
3. Lihat asal pemeriksaan, kondisi yang cocok, cabang yang dipilih, dan hasil setiap aksi.
4. Jika ada kegagalan, baca penyebab yang diketahui serta tindakan perbaikannya.
5. Kembali ke aturan terkait bila aturan tersebut masih tersedia.

## 11. Kebutuhan fungsional

Semua kebutuhan di bawah merupakan spesifikasi usulan v0.1. Kebutuhan yang diberi Must menjadi syarat MVP usulan, dengan dukungan platform dibuktikan pada perangkat yang dinyatakan didukung.

| ID | Prioritas | Kebutuhan | Kriteria penerimaan |
| --- | --- | --- | --- |
| FR-01 | Must | Pengelolaan aturan | Pengguna dapat membuat, membuka, mengubah, menjeda, mengaktifkan, dan menghapus aturan. |
| FR-02 | Must | Penyimpanan konfigurasi | Aturan yang disimpan tetap tersedia setelah aplikasi dibuka ulang. |
| FR-03 | Must | Editor sederhana | Pengguna dapat membuat Ketika → Lakukan tanpa menambahkan IF secara manual. |
| FR-04 | Must | Percabangan dasar | Pengguna dapat menambah, menghapus, dan mengurutkan IF/ELSE IF serta satu ELSE opsional. |
| FR-05 | Must | Pemilihan cabang yang jelas | Urutan cabang terlihat; hanya cabang pertama yang cocok dipilih dalam satu evaluasi. |
| FR-06 | Must | Pemicu area lokasi | Pengguna menentukan beberapa area yang digunakan aturan; perubahan area A ke B dapat memicu pemeriksaan. |
| FR-07 | Must | Pemicu Bluetooth terpilih | Aturan dapat menggunakan perubahan koneksi perangkat pilihan; perangkat lain tidak dianggap sebagai kecocokan. |
| FR-08 | Must | Aksi wallpaper | Pengguna memilih gambar dan sasaran layar yang didukung; kegagalan akses gambar atau pemasangan dijelaskan. |
| FR-09 | Must | Aksi volume media | Jenis volume dan nilai target terlihat sebelum aktivasi; hasil atau kegagalan pengaturan dicatat. |
| FR-10 | Must | Beberapa aksi berurutan | Aksi dicoba sesuai urutan editor; hasil dicatat per aksi dan dapat menghasilkan Sebagian berhasil. |
| FR-11 | Must | Validasi konfigurasi | Parameter tidak valid ditandai dekat bidang terkait; konfigurasi tidak lengkap dapat disimpan sebagai draf. |
| FR-12 | Must | Kesiapan dan izin | Keinginan aktif, kesiapan, dan hasil terakhir dibedakan; akses yang hilang tidak disamarkan sebagai aturan siap. |
| FR-13 | Must | Data kondisi tidak diketahui | Data yang hilang atau tidak cukup andal menghasilkan status belum dapat diperiksa, tanpa menjalankan ELSE karena asumsi. |
| FR-14 | Must | Simulasi logika | Pengguna memasukkan keadaan contoh dan melihat pilihan cabang tanpa mengubah pengaturan HP. |
| FR-15 | Must | Uji aksi | Pengguna dapat mencoba aksi secara eksplisit; pengujian dijelaskan sebagai perubahan nyata pada HP. |
| FR-16 | Must | Riwayat per eksekusi | Riwayat menunjukkan aturan, waktu, asal pemicu, cabang, serta hasil aksi dan penyebab yang diketahui. |
| FR-17 | Must | Pencegahan aksi ganda | Duplikasi laporan kejadian yang sama tidak membuat satu aksi dijalankan berulang tanpa alasan baru. |
| FR-18 | Must | Konflik antaraturan | Jika kandidat aksi pada evaluasi yang sama menulis sasaran yang sama, pemilihan mengikuti prioritas yang terlihat dan hasil konflik dapat diperiksa. |
| FR-19 | Must | Penghormatan perubahan manual | Mengubah volume atau wallpaper secara manual tidak memicu pemaksaan ulang nilai aturan tanpa evaluasi baru yang sah. |
| FR-20 | Must | Pemulihan pemantauan | Pemantauan yang diperlukan dipulihkan setelah restart perangkat jika platform mengizinkan dan akses tersedia; ketidaktersediaan dijelaskan. |
| FR-21 | Must | Penghapusan riwayat | Pengguna dapat menghapus riwayat tanpa menghapus aturan yang disimpan. |
| FR-22 | Must | Pengaturan penggunaan dasar | Tersedia pengaturan dan bantuan singkat untuk melihat kebutuhan akses serta perilaku aktivasi aturan. |
| FR-23 | Should | Contoh awal dan duplikasi | Pengguna dapat memulai dari contoh yang masih bisa diedit dan menggandakan aturan tanpa otomatis mengaktifkan salinannya. |
| FR-24 | Should | Filter dan pencarian | Daftar dapat dipersempit berdasarkan pemicu atau nama; riwayat dapat difilter menurut hasil. |
| FR-25 | Should | Pengendalian ukuran riwayat | Retensi riwayat dapat dibatasi dengan penjelasan yang jelas sebelum data lama dibersihkan. |
| FR-26 | Should | Durasi menetap di area | Pengguna dapat memilih kebutuhan menetap di area apabila pendekatan lokasi yang digunakan mendukungnya. |

## 12. Aturan perilaku produk

### 12.1 Evaluasi kondisi

Satu evaluasi memilih cabang pertama yang cocok. ELSE hanya dipilih apabila kondisi sebelumnya dapat dinilai dan semuanya tidak cocok. Jika kondisi yang menentukan pemilihan cabang belum dapat diketahui, evaluasi ditunda atau dilewati dengan alasan yang terlihat.

ELSE IF tidak dipakai untuk menyatakan dua kebutuhan independen yang harus berlaku bersamaan. Editor dan contoh awal perlu menghindari pola yang membuat pengguna mengira seluruh cabang selalu dijalankan.

### 12.2 Konflik antaraturan

Usulan MVP menggunakan urutan prioritas yang terlihat pada daftar aturan. Dalam satu kelompok evaluasi, kandidat aksi untuk sasaran yang sama dipilih dari aturan dengan prioritas lebih tinggi. Urutan yang ditampilkan harus cukup untuk menentukan pemenang tanpa hasil acak.

Peristiwa sah yang datang kemudian dapat menghasilkan perubahan baru. Prioritas tersebut tidak dimaksudkan sebagai penguncian pengaturan sepanjang hari. Definisi kelompok evaluasi, antrean, dan pemecahan konflik terperinci ditetapkan pada `RULE_ENGINE.md`.

### 12.3 Hasil yang sudah diterapkan

Menjeda aturan, menutup aplikasi, atau berakhirnya kondisi tidak otomatis memulihkan pengaturan sebelumnya. Perilaku awal mempertahankan hasil terakhir. Perilaku saat keluar kondisi harus dapat dipahami dari konfigurasi pengguna.

Pemulihan otomatis wallpaper asli, pengembalian volume sebelumnya, dan mekanisme profil yang saling menumpuk belum menjadi janji MVP.

### 12.4 Kegagalan dan pengulangan

Kegagalan suatu aksi dicatat. Usulan awal adalah menghentikan aksi berikutnya dalam cabang tersebut agar urutan yang bergantung pada aksi sebelumnya tidak diteruskan secara keliru. Aksi yang sudah berhasil tidak dianggap ikut gagal atau otomatis dibatalkan.

Krono tidak melakukan pengulangan aksi tanpa batas. Kebijakan percobaan ulang ditentukan per jenis aksi pada dokumen mesin aturan, dengan tetap mencegah pengulangan kejadian yang sama.

### 12.5 Pemulihan setelah gangguan

Konfigurasi tersimpan dan pemantauan perlu dipulihkan secara terpisah. Pemulihan setelah restart bukan alasan untuk selalu mengulang aksi yang sudah selesai. Aplikasi perlu memeriksa keadaan yang masih berlaku dan menghindari pemaksaan hasil lama pada perubahan manual pengguna.

Dukungan ketika aplikasi berada di background harus diuji. Keadaan Force Stop, akses dicabut, atau pembatasan sistem tidak boleh dilaporkan sebagai pemantauan yang pasti berjalan.

## 13. Kebutuhan tampilan dan informasi

### 13.1 Layar Aturan

Menampilkan nama aturan, ringkasan perilaku, keinginan aktif atau dijeda, kesiapan, dan hasil terakhir yang relevan. Kartu harus dapat dipahami tanpa membaca seluruh detail percabangan.

Contoh ringkasan adalah “2 lokasi · 2 aksi” atau “Sony terhubung → Volume media 20%”. Rincian kondisi dibuka melalui editor atau detail aturan.

Angka seperti jumlah aturan aktif harus memiliki definisi yang jelas. Jika satu aturan membutuhkan izin, tampilkan informasi seperti “2 siap · 1 perlu izin” agar pengguna tidak menganggap semuanya siap dijalankan.

### 13.2 Editor Aturan

Editor menggunakan susunan vertikal. Pemicu, kondisi, dan aksi dapat dibedakan melalui judul, ikon, serta warna. Warna tidak menjadi satu-satunya pembeda.

Urutan cabang dan aksi dapat diubah. Gambar memiliki pratinjau; pilihan volume menyebut jenisnya; pilihan lokasi menyebut nama area dan radius. Judul atau parameter yang terpotong tetap dapat diperiksa melalui detail yang mudah diakses.

Editor menyediakan Simulasikan dan Uji aksi dengan penjelasan dampak masing-masing. Ketersediaan opsi mengikuti kemampuan perangkat yang sudah diperiksa.

### 13.3 Layar Riwayat

Riwayat dikelompokkan per eksekusi. Informasi minimal adalah waktu, nama aturan saat dijalankan, alasan pemeriksaan, cabang yang dipilih, hasil setiap aksi, dan penyebab kegagalan yang diketahui.

Riwayat membedakan simulasi, uji aksi, evaluasi awal saat aktivasi, pemicu perangkat, dan pemulihan. Pesan untuk pengguna menggunakan bahasa yang jelas; detail teknis dapat dibuka bila diperlukan.

Riwayat rutin tidak perlu merekam setiap pembacaan lokasi atau sensor. Catatan harus cukup untuk memahami eksekusi tanpa menyimpan data yang berlebihan.

### 13.4 Keadaan yang perlu dibedakan

| Lapisan informasi | Contoh nilai | Makna |
| --- | --- | --- |
| Keinginan pengguna | Draf, Aktif, Dijeda | Apakah pengguna ingin aturan digunakan. |
| Kesiapan | Siap, Perlu izin, Data belum tersedia, Konfigurasi bermasalah, Tidak didukung | Apakah aturan dapat diperiksa atau dijalankan sekarang. |
| Keadaan eksekusi | Menunggu pemicu, Sedang berjalan | Apa yang sedang dilakukan mesin. |
| Hasil eksekusi terakhir | Berhasil, Sebagian berhasil, Gagal, Dilewati | Hasil satu upaya evaluasi atau eksekusi, terpisah dari kesiapan saat ini. |

Istilah final dan susunan visual ditetapkan pada `DESIGN.md`. Referensi awal menggunakan permukaan gelap, aksen ungu, cyan, dan hijau. Nilai warna, tipografi, radius, dan ukuran komponen perlu disatukan sebelum diimplementasikan.

## 14. Kebutuhan kualitas

| ID | Kebutuhan | Cara memeriksa |
| --- | --- | --- |
| NFR-01 | Mudah dipahami | Pengguna dapat menjelaskan pemicu dan hasil aturan dari ringkasan serta editor tanpa membaca kode. |
| NFR-02 | Aksesibel | Teks dapat membesar, informasi tidak bergantung pada warna, dan area sentuh mengikuti panduan Android. Acuan awal area sentuh minimal 48 × 48dp. [R7] |
| NFR-03 | Data lokal terlindungi | Konfigurasi, identitas perangkat, gambar, dan riwayat disimpan pada area aplikasi yang sesuai; penghapusan tersedia. |
| NFR-04 | Tidak bergantung pada server Krono | Pembuatan dan evaluasi aturan tidak membutuhkan akun atau backend Krono. Ketergantungan platform lokasi atau pemilihan peta tetap dinilai terpisah. |
| NFR-05 | Penggunaan daya terkendali | Pemantauan mengikuti API yang sesuai kebutuhan; tidak menetapkan pembacaan GPS terus-menerus sebagai perilaku awal. Dampak baterai diukur pada perangkat uji. |
| NFR-06 | Konsisten dan dapat ditelusuri | Masukan evaluasi yang sama menghasilkan pilihan cabang yang sama; konflik, duplikasi, dan kegagalan memiliki alasan yang dapat diperiksa. |
| NFR-07 | Tahan terhadap gangguan | Proses mati, restart, sumber daya hilang, dan pencabutan akses tidak menghilangkan konfigurasi tersimpan atau menghasilkan status keberhasilan palsu. |
| NFR-08 | Respons antarmuka jelas | Penyimpanan, pemuatan, dan kegagalan menampilkan keadaan yang sesuai; operasi perangkat tidak membekukan interaksi editor. |
| NFR-09 | Dukungan perangkat terukur | Fitur dinyatakan didukung berdasarkan dokumentasi dan hasil uji pada matriks perangkat/Android yang ditetapkan kemudian. |
| NFR-10 | Batas waktu eksekusi jujur | UI tidak menjanjikan waktu respons lokasi atau background yang belum terbukti dan tidak dikendalikan penuh oleh aplikasi. |

Telemetri cloud, pengiriman koordinat, atau pengiriman riwayat ke layanan eksternal tidak termasuk kebutuhan MVP usulan. Pengukuran awal dilakukan melalui skenario uji dan informasi lokal yang diperlukan.

## 15. Ketergantungan dan batas platform

### 15.1 Lokasi

Geofencing Android menggunakan titik pusat dan radius, mendukung transisi masuk/keluar atau menetap, serta memiliki kebutuhan akses lokasi. Dokumentasi juga menjelaskan bahwa pemberitahuan dapat terlambat dan pemantauan tertentu perlu didaftarkan ulang setelah restart. [R3]

Pilihan penyedia lokasi, kebutuhan Google Play services, pemilihan peta, radius yang layak, serta perilaku di perangkat tanpa layanan tersebut diperiksa pada `ANDROID_CAPABILITIES.md`. PRD ini tidak menetapkan kemampuan membedakan ruangan atau titik yang sangat berdekatan.

### 15.2 Bluetooth dan audio

Kebutuhan akses Bluetooth bergantung pada versi Android dan operasi yang dilakukan. Aturan untuk perangkat yang dipilih memerlukan jalur akses yang benar; keberhasilan membaca keadaan koneksi tidak disamakan dengan kemampuan mengendalikan seluruh pengaturan TWS. [R4]

Nama perangkat digunakan sebagai label tampilan, sementara pemilihan harus membedakan perangkat secara konsisten. Dukungan Bluetooth Classic, LE Audio, beberapa perangkat tersambung, dan pemetaan persentase volume masuk daftar verifikasi perangkat.

### 15.3 Wallpaper dan file gambar

Android menyediakan API pemasangan wallpaper. Dukungan pemasangan dan sasaran layar perlu diperiksa; pemasangan gambar tidak menjamin aplikasi dapat membaca kembali wallpaper lama untuk pemulihan. [R5]

Produk memerlukan akses gambar yang bertahan sesuai mekanisme penyimpanan yang dipilih. Jika gambar tidak bisa dibaca lagi, aturan menampilkan masalah sumber gambar dan tidak mencatat pemasangan sebagai berhasil.

### 15.4 Pengembangan kemampuan tambahan

DND, mode hemat daya, peluncuran aplikasi lain, dan tindakan khusus vendor hanya boleh ditambahkan setelah jalur akses, batasan, serta pengalaman pengguna yang mungkin terjadi telah diperiksa. Kebutuhan fitur baru dicatat sebagai perubahan cakupan yang terlihat.

## 16. Kriteria penerimaan MVP usulan

| ID | Skenario | Hasil yang diharapkan | Kebutuhan terkait |
| --- | --- | --- | --- |
| AC-01 | Buat aturan TWS lalu buka ulang aplikasi. | Nama perangkat, nilai volume, dan status aturan tetap tersimpan; salinan atau aturan baru tidak aktif tanpa pilihan pengguna. | FR-01, FR-02, FR-07, FR-09 |
| AC-02 | Sambungkan TWS yang dipilih, lalu sambungkan perangkat lain pada uji terpisah. | TWS pilihan memicu aksi yang benar; perangkat lain tidak dianggap sebagai pemicu yang cocok. | FR-07, FR-09, FR-16 |
| AC-03 | Terima perubahan area A, kemudian area B, termasuk saat keduanya berada di kawasan yang sama. | Gambar yang sesuai dipilih untuk tiap keadaan valid; pemantauan tidak berhenti pada kejadian masuk kawasan besar saja. | FR-04, FR-05, FR-06, FR-08 |
| AC-04 | Simulasikan kondisi A dan B sekaligus cocok. | Hanya cabang pertama yang cocok dipilih sesuai urutan editor. | FR-04, FR-05, FR-14 |
| AC-05 | Hilangkan data lokasi atau cabut akses yang diperlukan. | Kesiapan berubah dan alasan terlihat; ELSE tidak dipilih karena data yang tidak diketahui. | FR-12, FR-13 |
| AC-06 | Setelah aksi volume selesai, ubah volume secara manual dan buka ulang layar. | Nilai tidak terus dikembalikan hanya karena pengguna mengubahnya atau membuka layar. | FR-17, FR-19 |
| AC-07 | Laporkan kejadian yang sama berulang pada pengujian terkontrol. | Tidak ada aksi ganda yang tidak beralasan; riwayat cukup menjelaskan penanganannya. | FR-16, FR-17 |
| AC-08 | Dua aturan menghasilkan kandidat aksi untuk sasaran yang sama pada evaluasi yang sama. | Hasil mengikuti prioritas yang terlihat; aksi yang dilewati memiliki alasan. | FR-18 |
| AC-09 | Aksi pertama berhasil dan aksi berikutnya sengaja dibuat gagal pada pengujian. | Hasil Sebagian berhasil, rincian per aksi, dan penghentian aksi berikutnya tercatat sesuai kebijakan. | FR-10, FR-16 |
| AC-10 | Jalankan simulasi, lalu jalankan uji aksi secara terpisah. | Simulasi tidak mengubah HP; uji aksi mengubah sasaran yang dipilih dan diberi penanda pengujian. | FR-14, FR-15 |
| AC-11 | Jeda aturan sebelum pemicu berikutnya. | Pemicu berikutnya tidak menjalankan aturan yang dijeda; hasil sebelumnya tidak otomatis dipulihkan. | FR-01, FR-12 |
| AC-12 | Restart perangkat uji dengan akses yang masih tersedia. | Konfigurasi tetap ada, pemantauan dipulihkan sesuai dukungan platform, dan aksi lama tidak diulang tanpa alasan evaluasi yang sah. | FR-02, FR-17, FR-20 |
| AC-13 | Hapus riwayat lalu buka daftar aturan. | Riwayat yang dipilih terhapus dan aturan tetap tersedia. | FR-21 |

Tabel ini merupakan kontrak perilaku usulan, bukan laporan hasil uji. Skenario lokasi harus diperiksa melalui simulasi logika dan pengujian perangkat; simulasi saja tidak membuktikan pemantauan background berhasil.

## 17. Risiko produk dan respons rancangan

| Risiko | Dampak bagi pengguna | Respons dalam kebutuhan produk |
| --- | --- | --- |
| Pengguna mengira sakelar aktif berarti seluruh akses sudah siap. | Aturan terlihat aktif tetapi tidak berjalan. | Pisahkan keinginan aktif dari kesiapan dan tampilkan langkah perbaikan. |
| Dua area tumpang tindih atau dua aturan menulis sasaran yang sama. | Hasil terasa berubah tanpa alasan. | Tampilkan urutan cabang dan prioritas; catat kandidat yang dilewati. |
| Pengguna mengharapkan respons lokasi tanpa jeda. | Otomasi dianggap rusak saat laporan sistem terlambat. | Jelaskan model area dan batas respons; ukur pada perangkat yang didukung. |
| Sumber gambar tidak tersedia atau akses dicabut. | Aksi gagal setelah sebelumnya berjalan. | Periksa akses sumber dan tampilkan status yang sesuai. |
| Pemulihan proses mengulang hasil lama. | Perubahan manual pengguna tertimpa. | Bedakan pemulihan pemantauan, pemicu baru, dan aksi yang sudah selesai. |
| Contoh mockup dianggap sebagai janji seluruh fitur. | Cakupan membesar sebelum dukungan terbukti. | Tampilkan hanya kemampuan yang didukung dan pertahankan daftar Later. |
| Riwayat tumbuh terlalu besar. | Penyimpanan bertambah dan riwayat sulit dibaca. | Catat eksekusi yang relevan, sediakan penghapusan, dan rancang retensi. |

## 18. Keputusan yang masih terbuka

| ID | Hal yang belum final | Posisi saat ini | Dokumen penentu |
| --- | --- | --- | --- |
| O-01 | Platform rilis pertama dan cakupan versi Android | Android merupakan usulan kerja; versi minimum dan target belum ditentukan. | `ANDROID_CAPABILITIES.md`, `ARCHITECTURE.md` |
| O-02 | Teknologi implementasi | Kotlin + Compose merupakan kandidat, belum keputusan final. | `ARCHITECTURE.md` |
| O-03 | Penyedia lokasi, pemilihan peta, dan dukungan tanpa Google Play services | Perlu penelitian serta percobaan perangkat. | `ANDROID_CAPABILITIES.md` |
| O-04 | Sasaran wallpaper yang didukung | Pilihan layar utama, layar kunci, atau keduanya mengikuti dukungan yang diverifikasi. | `ANDROID_CAPABILITIES.md` |
| O-05 | Bluetooth Classic, LE Audio, serta beberapa keluaran audio | Pemicu perangkat tertentu menjadi kebutuhan inti; matriks dukungan belum dibuat. | `ANDROID_CAPABILITIES.md` |
| O-06 | Prioritas, kelompok evaluasi, dan batas pengulangan aksi | Perilaku produk diusulkan pada bagian 12; mekanisme tepatnya belum dikunci. | `RULE_ENGINE.md` |
| O-07 | Evaluasi awal ketika aktivasi atau penyimpanan revisi | Usulan memeriksa sekali dengan penjelasan sebelum aktivasi; perlu validasi UX dan teknis. | `RULE_ENGINE.md`, `DESIGN.md` |
| O-08 | Tema, bahasa antarmuka, dan ukuran komponen | Referensi gelap dan bahasa Indonesia menjadi arah awal; spesifikasi final belum dibuat. | `DESIGN.md` |
| O-09 | Retensi riwayat dan pilihan cadangan data | Penghapusan manual wajib; batas retensi serta ekspor/impor belum final. | `DATABASE.md`, `DESIGN.md` |
| O-10 | Distribusi, monetisasi, dan sasaran rilis | Belum diminta atau ditetapkan. | Revisi PRD dan rencana implementasi bila dibutuhkan. |
| O-11 | Target performa dan perangkat uji | Belum ada pengukuran yang dapat dijadikan angka target. | `TESTING.md` |

Keputusan terbuka tidak diisi dengan angka, dukungan, atau hasil uji rekaan. Pada tahap berikutnya, catat dasar keputusan dan dampaknya jika cakupan perlu berubah.

## 19. Hubungan dengan dokumen berikutnya

| Dokumen | Informasi yang diturunkan dari PRD |
| --- | --- |
| `ANDROID_CAPABILITIES.md` | Membuktikan kelayakan pemicu dan aksi, akses, versi, serta batas background. Ini menjadi pekerjaan berikutnya. |
| `RULE_ENGINE.md` | Merinci semantik cabang, data tidak diketahui, konflik, antrean, pengulangan, dan pemulihan. |
| `ARCHITECTURE.md` | Memilih teknologi dan pembagian tanggung jawab berdasarkan kebutuhan yang sudah diuji kelayakannya. |
| `DATABASE.md` | Menentukan bentuk penyimpanan dan hubungan data yang dibutuhkan produk. |
| `DESIGN.md` | Mengubah kebutuhan layar dan keadaan produk menjadi spesifikasi visual dan interaksi. |
| `TESTING.md` | Mengembangkan kriteria penerimaan menjadi skenario uji yang dapat dijalankan. |
| `IMPLEMENTATION_PLAN.md` | Mengurutkan pembangunan berdasarkan ketergantungan dan kriteria selesai. |
| `README.md` | Merangkum arah produk, status pekerjaan, serta cara membaca dokumentasi. |

Nama dokumen pada tabel adalah rencana keluaran berikutnya. PRD ini tidak menyatakan file-file tersebut sudah dibuat.

## 20. Referensi dan bahan sumber

### 20.1 Bahan dari pemilik produk

- Penjelasan konsep Krono, contoh lokasi/wallpaper, dan koneksi TWS/volume dalam pembahasan proyek ini.
- `DESIGN(1).md`: sistem desain awal dengan arah Android, permukaan gelap, dan komponen editor otomasi.
- `code(1).html` dan `screen(1).png`: referensi daftar aturan.
- `code(2).html` dan `screen(2).png`: referensi editor aturan.
- `code.html` dan `screen.png`: referensi riwayat eksekusi.
- Pembahasan evaluasi rancangan dan urutan pembuatan dokumen.

Lampiran digunakan sebagai bahan konsep dan visual. Kode HTML merupakan referensi antarmuka, bukan implementasi mesin otomasi Android yang telah diverifikasi.

### 20.2 Referensi resmi

Referensi di bawah diperiksa pada 22 Oktober 2026. Gunakan sumber resmi kembali saat mengunci API atau versi dependensi karena isinya dapat berubah. Tautan repository mengarah ke proyek resmi saat ini, bukan versi rilis yang diklaim paling baru.

| ID | Sumber | Kegunaan dalam PRD |
| --- | --- | --- |
| R1 | [Android: Recommendations for Android architecture](https://developer.android.com/topic/architecture/recommendations) | Dasar kandidat arsitektur dan toolkit UI; detail teknis belum dikunci. |
| R2 | [Repository resmi android/nowinandroid](https://github.com/android/nowinandroid) | Referensi implementasi Kotlin/Compose dan struktur aplikasi; tidak dianggap sebagai mesin aturan siap pakai. |
| R3 | [Android: Create and monitor geofences](https://developer.android.com/develop/sensors-and-location/location/geofencing) | Model area, transisi, akses lokasi, jeda pemberitahuan, dan pemulihan pemantauan. |
| R4 | [Android: Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions) | Kebutuhan akses Bluetooth sesuai versi dan operasi. |
| R5 | [Android: WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager) | Dasar kemampuan pemasangan wallpaper serta pemeriksaan dukungan. |
| R6 | [Android: AudioManager](https://developer.android.com/reference/android/media/AudioManager) | Dasar pengaturan tingkat volume pada jenis stream yang dipilih. |
| R7 | [Android: Accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) | Acuan aksesibilitas dan area sentuh komponen. |

## 21. Riwayat dokumen

| Versi | Tanggal | Perubahan |
| --- | --- | --- |
| 0.1 | 22 Oktober 2026 | PRD awal Krono berdasarkan kebutuhan pemilik produk, lampiran desain, evaluasi rancangan, dan referensi resmi. |

