# Krono

## Android Capabilities

| Atribut | Keterangan |
| --- | --- |
| Dokumen | `ANDROID_CAPABILITIES.md` |
| Versi | 0.1 |
| Tanggal pemeriksaan sumber | 22 Oktober 2026, Asia/Jakarta |
| Acuan produk | PRD Krono v0.1, 22 Oktober 2026 |
| Tahap | Penelitian kemampuan Android sebelum implementasi |
| Dasar akses | Aplikasi Android biasa dengan akses yang diberikan pengguna |
| Cakupan inti | Lokasi, koneksi TWS, wallpaper, volume media, dan eksekusi background |
| Bukti saat ini | Dokumentasi resmi dan repository; belum ada pengujian aplikasi Krono di perangkat |

> Tersedianya sebuah API belum membuktikan satu otomasi lengkap dapat berjalan dalam semua keadaan. Dokumen ini membedakan penerimaan pemicu, pembacaan kondisi, izin menjalankan aksi, dan pemeriksaan hasilnya.

## 1. Tujuan dan cara membaca

Dokumen ini memetakan kebutuhan PRD ke kemampuan Android. Hasilnya digunakan untuk merancang mesin aturan, memilih arsitektur, dan menentukan pengujian yang perlu dilakukan sebelum fitur dinyatakan didukung.

Dokumen ini tidak mengubah kebutuhan inti PRD dan tidak menyatakan bahwa purwarupa sudah dibuat. Temuan yang memerlukan penyesuaian produk dicatat pada bagian 14 sebagai masukan revisi berikutnya.

### 1.1 Status kemampuan

| Status | Makna |
| --- | --- |
| API tersedia | Ada jalur API publik untuk operasi tersebut. Hasil pada Krono masih perlu diuji. |
| Bersyarat | Operasi bergantung pada akses, layanan, keadaan aplikasi, atau dukungan perangkat. |
| Perlu PoC | Rangkaian penggunaan Krono memerlukan proof of concept, yaitu percobaan teknis kecil untuk membuktikan kelayakannya. |
| Akses standar tidak mencukupi | Jalur yang diperiksa memerlukan hak sistem atau akses di luar dasar MVP. |

Status di atas terpisah dari status pengujian. Semua skenario pengujian dalam dokumen ini berstatus **Belum dijalankan**. Kata didukung oleh dokumentasi tidak berarti sudah terbukti bekerja pada Krono.

### 1.2 Fakta dan usulan

- **Fakta platform** diberi referensi resmi, misalnya [S05].
- **Implikasi untuk Krono** menjelaskan kesimpulan rancangan dari fakta tersebut.
- **Usulan** merupakan pilihan implementasi atau pengalaman pengguna yang belum menjadi keputusan final pemilik produk.
- **Belum diketahui** dipertahankan sebagai ketidakpastian, bukan diubah menjadi kondisi salah atau hasil berhasil.

## 2. Ringkasan keputusan penelitian

Kedua contoh inti tetap menjadi sasaran produk. Namun, alur TWS → volume perlu pemeriksaan khusus pada Android 17. Sistem memperketat penggunaan API audio dari background, sehingga jalur eksekusinya harus dibuktikan sebelum dijanjikan berjalan otomatis saat aplikasi tidak terlihat. [S02][S03]

| ID | Kemampuan | Penilaian | Implikasi untuk Krono |
| --- | --- | --- | --- |
| CAP-01 | Memantau area lokasi melalui Google Play services | Bersyarat | Memerlukan akses dan layanan lokasi yang sesuai. |
| CAP-02 | Memeriksa perpindahan A ke B | API tersedia | Setiap area yang relevan harus ikut dipantau. |
| CAP-03 | Memilih TWS yang telah dipasangkan | Bersyarat | Mulai dari daftar perangkat yang sudah dipasangkan dan akses Bluetooth yang diperlukan. |
| CAP-04 | Menerima perubahan koneksi A2DP | Bersyarat | Kandidat utama pemicu TWS Classic; uji saat proses aplikasi tidak hidup. |
| CAP-05 | Memantau TWS LE Audio | Perlu PoC | Penanganan profil dan dua sisi earbud harus diperiksa terpisah. |
| CAP-06 | Mengatur volume media ketika UI terlihat | API tersedia | Tetap periksa batas volume perangkat dan hasil perubahan. |
| CAP-07 | Mengatur volume media dari background pada Android 17 | Perlu PoC | Harus memenuhi persyaratan lifecycle audio; penerimaan pemicu saja belum cukup. |
| CAP-08 | Memasang gambar wallpaper | Bersyarat | Periksa dukungan, izin pemasangan, sasaran layar, dan akses gambar. |
| CAP-09 | Membaca wallpaper lama untuk pemulihan | Akses standar tidak mencukupi pada Android modern | Jangan menjanjikan pemulihan wallpaper asli secara otomatis. |
| CAP-10 | Mengakses gambar pilihan setelah restart | Bersyarat | Pertahankan akses URI atau impor salinan yang dikelola aplikasi. |
| CAP-11 | Memulihkan pemantauan setelah restart | Bersyarat | Pisahkan pemulihan pemicu dari pengulangan aksi. |
| CAP-12 | Terus berjalan setelah pengguna melakukan Force Stop | Tidak menjadi kemampuan yang dijanjikan | Tunggu interaksi pengguna yang membuka kembali aplikasi. |
| CAP-13 | Mengatur DND | Bersyarat, Later | Memerlukan akses kebijakan notifikasi dan model aturan yang sesuai. |
| CAP-14 | Menyalakan Battery Saver sistem langsung | Akses standar tidak mencukupi | Tidak masuk janji fitur MVP. |
| CAP-15 | Membuka Maps atau aplikasi lain dari background | Bersyarat, Later | Pertimbangkan interaksi melalui notifikasi bila peluncuran tidak diizinkan. |

Referensi rinci dan batas setiap kemampuan dijelaskan pada bagian 5 sampai 11. Penilaian ini tidak mengharuskan seluruh fitur dinonaktifkan ketika satu kemampuan tidak tersedia.

## 3. Versi Android dan batas penetapan SDK

Tiga hal perlu dibedakan: versi Android yang sedang dipakai HP, `minSdk` sebagai batas pemasangan aplikasi, dan `targetSdk` sebagai versi perilaku yang ditargetkan aplikasi.

Android 17 menggunakan API level 37. Dokumentasi resminya membedakan perubahan untuk semua aplikasi yang berjalan di Android 17 dan perubahan tambahan untuk aplikasi yang menargetkan API 37. Menurunkan target tidak menghapus seluruh perubahan runtime. [S01][S02][S03]

| Versi / API | Hal yang relevan untuk rancangan | Sumber |
| --- | --- | --- |
| Android 8 / 26 | Pembatasan receiver implisit perlu diperhatikan. | [S13] |
| Android 10 / 29 | Akses lokasi background menjadi bagian penting alur geofence. | [S05][S08] |
| Android 11 / 30 | Pemberian akses lokasi sepanjang waktu diarahkan melalui halaman pengaturan. | [S08] |
| Android 12 dan 12L / 31–32 | Izin Nearby devices, pilihan lokasi perkiraan, mutabilitas PendingIntent, dan batas memulai FGS memengaruhi alur. | [S07][S09][S06][S21] |
| Android 13 / 33 | Izin notifikasi runtime perlu ditangani; LE Audio memiliki dukungan platform yang dibahas tersendiri. | [S23][S12] |
| Android 14 / 34 | Tipe FGS perlu tepat; akses membaca wallpaper lama makin dibatasi. | [S22][S18] |
| Android 15 / 35 | Force Stop membatalkan PendingIntent; perilaku DND juga bergantung pada target aplikasi. | [S24][S27] |
| Android 16 / 36 | Menjadi kandidat uji pembanding perangkat sebelum perubahan audio Android 17. Ini sasaran uji, bukan klaim tidak ada perubahan lain. | Usulan matriks uji |
| Android 17 / 37 | Validasi lifecycle untuk operasi audio background menjadi kebutuhan penting. | [S02][S03][S04] |

**Posisi saat ini:** minSdk belum dikunci. API 37 menjadi kandidat target evaluasi karena termasuk platform terkini yang diperiksa, tetapi keputusan build dan distribusi berada pada `ARCHITECTURE.md`. Dukungan versi lama baru dinyatakan setelah diuji. API yang hanya ada pada preview atau SDK extension tertentu tidak dianggap tersedia pada semua perangkat dengan nomor Android yang sama.

## 4. Matriks izin dan akses

Izin dikelompokkan menurut fitur. Pengguna yang hanya membuat aturan TWS tidak perlu dipaksa memberikan akses lokasi untuk mengaktifkan konfigurasi Bluetooth tersebut.

| Akses | Jenis / konteks | Kegunaan | Posisi MVP |
| --- | --- | --- | --- |
| `ACCESS_COARSE_LOCATION` dan `ACCESS_FINE_LOCATION` | Runtime | Pemilihan dan penggunaan lokasi dengan ketepatan yang sesuai; ikuti permintaan berpasangan pada Android modern. | Untuk fitur lokasi. [S07] |
| `ACCESS_BACKGROUND_LOCATION` | Runtime dengan alur khusus | Pemantauan lokasi ketika UI tidak terlihat. | Untuk otomasi lokasi background. [S05][S08] |
| `BLUETOOTH_CONNECT` | Runtime pada Android 12+ | Akses yang relevan terhadap perangkat yang sudah dipasangkan dan profil koneksi. | Untuk fitur TWS. [S09][S10] |
| `BLUETOOTH` | Izin lama | Kompatibilitas jalur Bluetooth pada versi sebelum izin modern. | Jika versi tersebut didukung. [S09] |
| `BLUETOOTH_SCAN` | Runtime pada Android 12+ | Pencarian aktif perangkat. | Hanya jika fitur pencarian aktif ditambahkan. [S09] |
| `BLUETOOTH_ADMIN` | Izin lama untuk operasi terkait | Discovery atau operasi lama yang benar-benar digunakan. | Tidak ditambahkan hanya karena ada contoh kode lama. [S09] |
| `SET_WALLPAPER` | Normal, dideklarasikan pada manifest | Memasang wallpaper. | Untuk aksi wallpaper. [S17] |
| `MODIFY_AUDIO_SETTINGS` | Normal, dideklarasikan pada manifest | Pengaturan audio global yang diizinkan. | Untuk aksi volume; bukan pengganti syarat lifecycle. [S17] |
| Akses URI gambar | Akses terhadap item yang dipilih | Membaca gambar sumber. | Gunakan pemilih sistem; pertahankan akses jika perlu. [S19] |
| `POST_NOTIFICATIONS` | Runtime pada Android 13+ | Menampilkan notifikasi yang memerlukan izin tersebut. | Diminta dalam konteks penggunaan notifikasi. [S23] |
| `RECEIVE_BOOT_COMPLETED` | Deklarasi manifest | Menerima kesempatan pemulihan setelah boot. | Untuk pemulihan yang dirancang. [S13] |
| `FOREGROUND_SERVICE` dan izin sesuai tipe FGS | Deklarasi sesuai versi dan tipe | Menjalankan pekerjaan FGS yang sah. | Hanya jika jalur FGS dipilih dan memenuhi syarat. [S22] |
| Akses kebijakan notifikasi | Akses khusus melalui pengaturan | Mengelola DND sesuai dukungan platform. | Later. [S27] |

Izin normal tidak memiliki dialog persetujuan runtime seperti izin lokasi. Memberikan izin tidak otomatis membuat semua operasi yang memakai izin itu selalu diizinkan. [S17]

### 4.1 Kekeliruan konfigurasi yang perlu dihindari

Contoh Bluetooth sering memberi `maxSdkVersion="30"` pada izin lokasi ketika lokasi hanya dipakai untuk discovery lama. Krono juga memiliki fitur geofence. Karena itu, jangan menyalin pembatasan tersebut ke izin lokasi utama Krono dan membuat fitur lokasi kehilangan jalur akses pada Android 12+. [S09]

`neverForLocation` berkaitan dengan penggunaan hasil scan Bluetooth untuk memperoleh lokasi. Penggunaan GPS pada fitur lain tidak menjadi alasan otomatis untuk mengaktifkan atau menonaktifkan atribut ini. Keputusan mengikuti penggunaan scan yang sebenarnya. [S09]

MVP tidak membutuhkan akses seluruh file, aksesibilitas, mikrofon, kamera, atau izin alarm hanya untuk mendukung dua contoh inti. Penambahan akses mengikuti operasi nyata yang dipilih, bukan sebagai cara umum untuk mempertahankan proses.

## 5. Kemampuan lokasi

### 5.1 Fakta platform

Geofence berbentuk area dengan titik pusat dan radius. API menyediakan kejadian masuk, keluar, serta menetap. Batas layanan yang didokumentasikan adalah 100 geofence aktif per aplikasi per pengguna perangkat. Panduan memberi acuan radius minimum sekitar 100–150 meter untuk hasil yang baik; ini bukan jaminan ketepatan. [S05]

Pemberitahuan dapat tertunda beberapa menit. Kondisi jaringan dan penyedia lokasi juga berpengaruh. Pemantauan perlu didaftarkan ulang pada keadaan tertentu, termasuk restart perangkat. Angka keterlambatan dalam panduan bukan SLA Krono. [S05]

### 5.2 Jalur kandidat

Gunakan `GeofencingClient` dari Google Play services sebagai kandidat awal. Registrasi memakai `GeofencingRequest` dan `PendingIntent`; penghapusan dapat memakai ID geofence. Pada Android S+, PendingIntent untuk jalur ini harus mutable sesuai kontrak API. Registrasi dapat hilang ketika aplikasi direset, diperbarui, atau PendingIntent dibatalkan. [S06]

Implikasi untuk Krono:

1. Pantau area A dan B sebagai area yang relevan. Perpindahan internal tidak cukup diwakili oleh satu pemicu masuk kawasan kampus.
2. Daftar area aktif dibentuk dari aturan yang benar-benar digunakan. Area yang sama dapat dipakai bersama agar tidak menghabiskan kapasitas secara berulang.
3. Catat hasil registrasi. Aturan tidak dianggap siap hanya karena konfigurasi sudah tersimpan.
4. Jika layanan yang dibutuhkan tidak tersedia, tandai kemampuan lokasi tersebut tidak tersedia dan pertahankan fitur lain.
5. Intent geofence diarahkan secara eksplisit ke komponen aplikasi. Mutabilitas tidak menjadi alasan menerima peristiwa tanpa validasi.

Penyedia peta untuk memilih area merupakan keputusan terpisah. Tersedianya Google Play services di sebuah HP harus diperiksa, bukan diasumsikan dari merek atau nomor Android.

### 5.3 Alur akses lokasi

Usulan alur pengguna: pilih fitur lokasi, jelaskan kebutuhan foreground, minta akses yang sesuai, kemudian jelaskan kebutuhan background secara terpisah. Pada Android 11+, akses sepanjang waktu diperoleh melalui halaman pengaturan. Pilihan lokasi perkiraan juga memengaruhi lokasi background. [S07][S08]

Pengguna tetap dapat memakai bagian lain dari Krono jika menolak akses lokasi. Untuk geofencing yang memerlukan akses presisi, akses perkiraan tidak ditampilkan sebagai kesiapan penuh.

### 5.4 Keadaan tidak diketahui

Usulan model kondisi adalah Dalam area, Di luar area, dan Belum diketahui. Perubahan layanan, hilangnya data, atau registrasi yang belum selesai tidak diubah menjadi Di luar area.

ELSE hanya dapat dipertimbangkan ketika kondisi yang diperlukan sudah diketahui. Riwayat boleh menjelaskan penundaan pemeriksaan, tetapi tidak perlu menyimpan seluruh perjalanan pengguna.

Mode tanpa backend Krono tidak sama dengan jaminan lokasi selalu bekerja tanpa jaringan. Penerimaan peristiwa perlu diuji pada kondisi jaringan yang dicantumkan dalam matriks perangkat.

## 6. Kemampuan Bluetooth dan TWS

### 6.1 Pemicu TWS Classic

Kandidat awal adalah `BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED`. Peristiwa memuat perangkat serta keadaan profil saat ini dan sebelumnya. Keadaan CONNECTING tidak dianggap sama dengan CONNECTED. [S10]

Broadcast A2DP, headset, dan beberapa kejadian ACL tercantum dalam pengecualian pembatasan receiver implisit. Hal ini menyediakan jalur kandidat penerimaan kejadian ketika UI tidak terbuka, tetap dengan akses dan kondisi sistem yang berlaku. [S13]

Implikasi untuk Krono:

- Pemilihan awal menggunakan perangkat yang telah dipasangkan agar tidak membutuhkan pemindaian terus-menerus.
- Nama digunakan untuk tampilan. Identitas internal harus membedakan perangkat dengan nama sama dan menangani kebutuhan memilih ulang setelah pemasangan ulang.
- A2DP, headset panggilan, dan ACL tidak dianggap sebagai kejadian audio yang identik. Normalisasi harus mencegah satu koneksi menghasilkan beberapa aksi yang tidak diperlukan.
- Izin menerima kejadian tidak disamakan dengan izin memulai FGS atau menjalankan aksi audio.
- Konfigurasi receiver mengikuti asal pengirim dan persyaratan platform, dengan validasi action serta isi Intent. [S14]

### 6.2 LE Audio

Dokumentasi LE Audio menyebut dukungan platform pada Android 13, sedangkan beberapa konstanta API sudah muncul sebelumnya. Nomor API sebuah konstanta tidak membuktikan seluruh perangkat menyediakan jalur audio yang diperlukan. [S12]

Profil `BluetoothLeAudio` memakai `ACTION_LE_AUDIO_CONNECTION_STATE_CHANGED`. Pada perangkat binaural, earbud kiri dan kanan dapat memiliki laporan keadaan masing-masing. [S11]

Usulan awal: buktikan A2DP terlebih dahulu, lalu uji LE Audio sebagai jalur tersendiri. Pengelompokan kedua earbud, koneksi sebagian, dan perpindahan transport harus menghasilkan satu keadaan pengguna yang masuk akal. Dukungan LE Audio belum dapat diklaim hanya karena pengujian Classic berhasil.

### 6.3 Perangkat terhubung dan keluaran audio aktif

Kebutuhan produk menyebut TWS tertentu terhubung. Itu tidak otomatis berarti pengaturan volume publik dapat diarahkan secara eksklusif ke perangkat tersebut ketika beberapa keluaran tersedia.

Usulan aksi MVP tetap bernama Atur volume media. API stream tidak dijadikan janji pengaturan volume independen setiap TWS. Jika keluaran yang relevan belum dapat dipastikan pada konfigurasi tertentu, statusnya masuk verifikasi perangkat, bukan langsung ditampilkan sebagai berhasil.

### 6.4 Companion Device Manager

`CompanionDeviceManager` menyediakan proses asosiasi perangkat dan memiliki jalur akses background tertentu. Asosiasi tidak otomatis membuat koneksi atau pemindaian berkelanjutan. [S26]

Kandidat ini dapat dievaluasi jika asosiasi eksplisit memang sesuai pengalaman Krono. Kemampuan memulai layanan dari background tetap perlu dibedakan dari kemampuan audio WIU pada Android 17. Belum ada bukti dalam pekerjaan ini bahwa asosiasi TWS saja menyelesaikan seluruh alur tersebut.

## 7. Kemampuan volume media

### 7.1 Operasi dasar

Kandidat API adalah `AudioManager` untuk `STREAM_MUSIC`. Ambil batas minimum dan maksimum yang berlaku, gunakan `setStreamVolume()`, lalu baca keadaan hasil. Perangkat dengan kebijakan volume tetap dapat mengabaikan perubahan. Metode setter tidak mengembalikan status keberhasilan berbentuk boolean. [S16]

Usulan pemetaan nilai produk:

```text
persentase = nilai pilihan pengguna antara 0 dan 100
target = pembulatan(minimum + persentase / 100 × (maksimum − minimum))
```

Pemetaan harus mengikuti rentang perangkat. Jangan menganggap semua HP mempunyai tepat 15 tingkat volume. Persentase menggambarkan pilihan pengaturan, bukan persentase kekuatan suara fisik di telinga.

Hindari mengubah volume nada dering, alarm, dan panggilan melalui asumsi bahwa semuanya mengikuti volume media. Perilaku aksesori dan volume Bluetooth absolut memerlukan pemeriksaan perangkat.

### 7.2 Perubahan penting Android 17

**Fakta platform:** semua aplikasi yang berjalan di Android 17 memerlukan activity terlihat atau FGS yang bukan `SHORT_SERVICE` untuk operasi audio background yang dibatasi. Target API 37 menambahkan persyaratan WIU pada FGS. Pengecualian alarm terkait stream alarm dan izin yang sesuai, bukan akses umum untuk volume media. Panggilan volume yang tidak memenuhi syarat dapat diabaikan tanpa exception. [S04]

**Kesimpulan untuk Krono:** receiver Bluetooth → panggil setter → tampilkan Berhasil belum menjadi rancangan yang cukup. Turunnya target SDK juga tidak menghapus seluruh batas runtime tersebut. [S02][S03]

### 7.3 Jalur yang perlu dibuktikan

| Konteks | Posisi rancangan |
| --- | --- |
| Pengguna menekan Uji aksi saat UI Krono terlihat | Kandidat pengujian dasar untuk API dan pemetaan volume. |
| Event TWS diterima pada receiver tanpa sesi eksekusi audio yang memenuhi syarat | Jangan menjanjikan perubahan volume background pada Android 17. |
| Sesi otomasi audio dimulai pengguna ketika UI terlihat, memakai FGS yang benar | Kandidat PoC utama; jenis layanan, umur sesi, dan pemulihan tetap harus dibuktikan. |
| Layanan dimulai kembali setelah boot atau proses mati | Tidak dianggap otomatis memiliki seluruh kelayakan sesi sebelumnya. |
| Pengguna membuka Krono atau berinteraksi melalui notifikasi yang sah | Kandidat pemulihan dengan keterlibatan pengguna; berbeda dari otomasi penuh tanpa interaksi. |

`connectedDevice` dapat diteliti sebagai kandidat tipe FGS untuk fungsi perangkat eksternal yang memang sesuai. Pemenuhan prasyarat deklarasi tipe belum membuktikan kecocokan penggunaan Krono. Jangan memilih `mediaPlayback` hanya karena aksi yang dilakukan berhubungan dengan volume. [S22]

**Usulan produk:** bila sesi audio perlu diaktifkan pengguna, jelaskan sebagai mode otomasi audio dengan cara menghentikan yang mudah. Selama jalur tersebut belum terbukti, kemampuan volume background tetap berstatus Perlu PoC. Ini tidak mengubah kemampuan menyusun aturan dan menguji aksi ketika UI terlihat.

### 7.4 Pemeriksaan hasil dan perubahan manual

Sebelum aksi, pastikan kejadian masih relevan dan target masih dapat diperiksa. Setelah permintaan perubahan, baca keadaan yang tersedia dalam jangka pengamatan terbatas. Hasil yang sudah sama dengan target sebelum permintaan dapat dicatat sebagai Tidak perlu perubahan.

Jika keadaan tidak dapat diverifikasi, jangan menaikkan hasil menjadi Berhasil hanya karena tidak ada exception. Pembacaan tingkat volume sistem juga tidak membuktikan tingkat suara fisik pada seluruh aksesori.

Pengamatan dibatasi untuk memeriksa satu upaya, bukan menjaga nilai terus-menerus. Perubahan manual berikutnya tetap dihormati sesuai PRD.

## 8. Kemampuan wallpaper dan gambar

### 8.1 Pemasangan

`WallpaperManager` menyediakan pemasangan gambar dengan sasaran `FLAG_SYSTEM`, `FLAG_LOCK`, atau kombinasi yang didukung. Periksa `isWallpaperSupported()` dan `isSetWallpaperAllowed()` terlebih dahulu. Overload pemasangan yang mengembalikan ID dapat memberikan nol saat gagal. [S18]

Usulan Krono: tawarkan sasaran layar setelah pemeriksaan dukungan. Catat respons pemasangan dan lakukan pemeriksaan visual pada perangkat uji untuk memastikan sasaran benar. Kerjakan pembacaan atau pengolahan gambar di luar thread UI.

### 8.2 Pemulihan wallpaper lama

Pembacaan gambar wallpaper saat ini dibatasi pada Android modern; dokumentasi menerangkan perubahan sejak Android 13 dan pembatasan lebih lanjut pada Android 14. Memiliki izin memasang tidak otomatis memberikan akses membaca gambar lama. [S18]

Karena itu, usulan MVP mempertahankan wallpaper terakhir saat keluar kondisi. Alternatif yang dapat dipilih pengguna adalah gambar cadangan miliknya sendiri. Menyimpan gambar yang pernah dipilih dalam Krono tidak sama dengan mengambil wallpaper asli yang dipasang aplikasi lain.

### 8.3 Pemilih gambar dan akses jangka panjang

Gunakan Photo Picker atau fallback sistem yang sesuai. Pemilih memberi akses ke gambar yang dipilih; akses sementara dapat perlu dipertahankan melalui `takePersistableUriPermission()` untuk penggunaan setelah aplikasi berhenti atau perangkat restart. [S19]

Usulan awal adalah mengimpor salinan gambar pilihan ke penyimpanan yang dikelola Krono setelah pengguna memilihnya. Ini memudahkan pengelolaan sumber untuk otomasi berikutnya. Alternatif URI persisten tetap dapat dipilih pada arsitektur, dengan pemeriksaan sumber yang hilang.

Gambar yang masih berada di penyedia cloud perlu berhasil dibaca saat disiapkan. Jangan menandai aset siap hanya karena URI telah diterima. Penghapusan aturan atau penggantian gambar perlu mempertimbangkan aset yang dipakai beberapa aturan; rincian berada pada `DATABASE.md`.

## 9. Pekerjaan background, receiver, dan pemulihan

### 9.1 Pisahkan tiga keputusan

1. Apakah sistem dapat mengirimkan kejadian ke Krono?
2. Apakah komponen yang menerima kejadian mempunyai waktu dan konteks untuk memprosesnya?
3. Apakah aksi yang dipilih diizinkan pada konteks eksekusi tersebut?

Pengecualian broadcast tidak otomatis menjadi pengecualian memulai FGS. Android 12+ membatasi peluncuran FGS dari background, dengan pengecualian tertentu; kebutuhan WIU juga mempunyai pembatasan sendiri. [S21]

### 9.2 Pembagian kandidat mekanisme

| Mekanisme | Penggunaan yang layak dievaluasi | Hal yang tidak boleh diasumsikan |
| --- | --- | --- |
| Receiver | Menerima kejadian, memvalidasi, mencatat, dan menyerahkan pekerjaan. | Proses tetap hidup tanpa batas setelah `onReceive()` selesai. |
| `goAsync()` | Memindahkan pekerjaan penerimaan yang singkat dari thread utama. | Menjadi layanan permanen atau memperpanjang pekerjaan tanpa batas. |
| WorkManager satu kali | Pekerjaan lokal yang dapat dijadwalkan dan dipulihkan, misalnya kandidat pemrosesan aset atau aksi wallpaper. | Selalu mulai seketika atau otomatis mendapat kelayakan audio. |
| WorkManager periodik | Pemeliharaan yang toleran terhadap waktu. | Polling lokasi atau TWS setiap beberapa detik. |
| FGS | Pekerjaan yang sesuai tipe dan diketahui pengguna, bila memang dibutuhkan. | Semua pembatasan background, WIU, atau pembatasan vendor otomatis hilang. |
| UI terlihat | Konfigurasi, penjelasan akses, dan uji aksi yang disengaja. | Kemampuan yang sama pasti tersedia setelah UI hilang. |

Receiver memiliki batas waktu; `goAsync()` tetap berada dalam batas penerimaan broadcast. Pekerjaan berat perlu diserahkan ke fasilitas yang sesuai. [S15]

WorkManager periodik memiliki interval minimum 15 menit dan waktu aktual mengikuti penjadwalan sistem. Pekerjaan expedited juga tunduk pada kuota. [S20]

Pilihan worker untuk sebuah aksi tetap mempertimbangkan apakah kejadian sudah kedaluwarsa ketika pekerjaan mulai berjalan. Mekanisme penjadwalan tidak mengubah kontrak kondisi dan urutan aksi pada PRD.

### 9.3 Notifikasi

Android 13+ memiliki izin notifikasi runtime. Izin `POST_NOTIFICATIONS` tidak diwajibkan hanya untuk memulai FGS, tetapi FGS tetap harus menyertakan notifikasi sesuai ketentuan sistem. [S23]

Implikasi untuk Krono: penolakan izin notifikasi tidak otomatis berarti izin volume atau Bluetooth hilang. Namun, kemampuan menampilkan ajakan melanjutkan sesi melalui notifikasi perlu dinilai terpisah. Jangan menggabungkan seluruh keadaan tersebut menjadi satu label Perlu izin tanpa penjelasan.

### 9.4 Matriks gangguan

| Keadaan | Perilaku yang dirancang | Batas yang dijelaskan |
| --- | --- | --- |
| UI ditutup | Aturan tetap tersimpan; pemantauan mengikuti mekanisme terdaftar. | Bukan jaminan proses terus hidup. |
| Proses dimatikan sistem | Rekonsiliasi konfigurasi dan sumber kejadian saat kesempatan berikutnya tersedia. | Tidak mengulang semua aksi terakhir. |
| Perangkat restart | Pulihkan pemantauan sesudah data dan akses tersedia. | Sesi audio perlu diperiksa terpisah. |
| Aplikasi diperbarui | Periksa registrasi serta aset yang masih diperlukan. | Jangan menganggap semua registrasi pasti bertahan. |
| Force Stop | Tampilkan kebutuhan melanjutkan saat pengguna kembali berinteraksi. | Tidak dijanjikan berjalan melewati pilihan berhenti pengguna. |
| Clear data atau uninstall | Perlakukan data lokal sebagai hilang. | Tidak dijanjikan pulih tanpa cadangan yang memang dibuat. |
| Izin dicabut | Perbarui kesiapan dan hentikan penggunaan akses yang hilang. | Data tidak diketahui tidak memicu ELSE. |
| Doze atau pembatasan baterai | Catat keterlambatan dan uji perilaku perangkat. | Pengecualian baterai tidak menjadi jaminan seluruh operasi tersedia. |

Android 15 membatalkan PendingIntent ketika aplikasi masuk stopped state. Setelah interaksi pengguna mengeluarkannya dari keadaan tersebut, ada kesempatan registrasi ulang melalui mekanisme yang dijelaskan platform. [S24]

Doze dan App Standby dapat menunda pekerjaan CPU atau jaringan background. Pengujian perlu membedakan dampak platform dari pembatasan tambahan vendor. [S25]

**Usulan MVP:** pulihkan pemantauan setelah perangkat pertama kali dibuka bila data aplikasi menggunakan penyimpanan yang memerlukan unlock. Jangan menjanjikan eksekusi sebelum unlock tanpa desain Direct Boot yang terpisah.

## 10. Pelaporan kesiapan dan hasil

Kesiapan perlu dilaporkan untuk setiap kemampuan yang digunakan aturan. Izin yang lengkap belum tentu berarti layanan sumber tersedia atau aksi bisa dijalankan pada konteks saat ini.

### 10.1 Contoh alasan teknis dan pesan pengguna

Kode di bawah merupakan usulan kontrak internal, bukan error code bawaan Android.

| Alasan | Pesan pengguna yang diusulkan | Respons |
| --- | --- | --- |
| `LOCATION_PERMISSION_REQUIRED` | Izinkan lokasi untuk aturan ini. | Arahkan ke alur akses yang sesuai. |
| `BACKGROUND_LOCATION_REQUIRED` | Izinkan pemantauan lokasi saat Krono tidak dibuka. | Jelaskan kebutuhan dan pilihan pengguna. |
| `LOCATION_PROVIDER_UNAVAILABLE` | Layanan lokasi belum tersedia. | Pertahankan data sebagai belum diketahui. |
| `LOCATION_REGISTRATION_FAILED` | Area belum berhasil dipantau. | Tampilkan alasan yang diketahui dan kesempatan memperbaiki. |
| `BLUETOOTH_PERMISSION_REQUIRED` | Izinkan akses perangkat Bluetooth. | Jangan meminta lokasi sebagai pengganti. |
| `BLUETOOTH_STATE_UNKNOWN` | Koneksi perangkat belum dapat diperiksa. | Tunggu keadaan yang valid. |
| `AUDIO_EXECUTION_CONTEXT_REQUIRED` | Lanjutkan mode otomasi audio. | Jelaskan tindakan yang diperlukan jika jalur sesi sudah terbukti. |
| `AUDIO_CHANGE_NOT_CONFIRMED` | Perubahan volume belum dapat dipastikan. | Hindari label berhasil palsu. |
| `WALLPAPER_NOT_ALLOWED` | Perangkat atau profil ini membatasi wallpaper. | Nonaktifkan aksi terkait pada keadaan tersebut. |
| `IMAGE_SOURCE_UNAVAILABLE` | Gambar sumber tidak dapat dibaca. | Minta pengguna memilih atau memperbaiki sumber. |
| `MONITORING_RESUME_REQUIRED` | Buka Krono untuk melanjutkan pemantauan. | Pulihkan setelah interaksi yang sah. |
| `CAPABILITY_NOT_VALIDATED` | Kemampuan ini belum diuji pada konfigurasi tersebut. | Jangan tampilkan sebagai dukungan yang sudah terbukti. |

### 10.2 Bukti hasil

Pisahkan hasil aturan dan hasil aksi. Satu aksi dapat berhasil, gagal, dilewati, tidak memerlukan perubahan, atau belum dapat diverifikasi. Gabungan aksi menghasilkan ringkasan sesuai bukti yang tersedia.

Penerimaan Intent, registrasi berhasil, setter dipanggil, dan sasaran berubah merupakan kejadian yang berbeda. Simpan informasi secukupnya agar perbedaannya dapat dilihat. Catatan simulasi juga tetap terpisah dari eksekusi nyata.

Koordinat lengkap, alamat perangkat, isi gambar, dan keluaran diagnostik luas tidak harus masuk pesan riwayat rutin. Gunakan nama area atau perangkat yang dipilih agar penjelasan tetap berguna.

## 11. Kemampuan lanjutan pada mockup

### 11.1 DND

Pengelolaan kebijakan notifikasi memerlukan akses khusus. Untuk aplikasi yang menargetkan Android 15+, pemanggilan API terkait umumnya mengaktifkan atau menonaktifkan `AutomaticZenRule` milik aplikasi, dengan pengecualian tertentu yang didokumentasikan. Ini bukan asumsi penggantian bebas seluruh kebijakan global. [S27]

Posisi Krono tetap Later. Desain perlu menjelaskan dampak aturan Krono sendiri dan menghormati pengaturan lain yang tetap berlaku.

### 11.2 Battery Saver dan mode vendor

Sumber AOSP yang diperiksa melindungi perubahan mode hemat daya melalui `POWER_SAVER` atau `DEVICE_POWER`; izin tersebut bukan izin runtime umum untuk aplikasi biasa. [S28][S29]

Karena itu, tombol Battery Saver ON dan Ultra Low Power pada mockup belum menjadi kemampuan MVP. Kemungkinan membuka halaman pengaturan atau memakai integrasi khusus merupakan fitur berbeda yang harus diteliti tersendiri. Sumber AOSP cabang `main` dipakai sebagai bukti mekanisme internal, bukan janji kontrak API publik pada semua vendor.

### 11.3 Membuka Maps atau aplikasi lain

Peluncuran activity dari background dibatasi Android. Diperbolehkannya satu komponen berjalan tidak otomatis mengizinkan komponen itu membuka layar aplikasi lain. [S30]

Usulan untuk penelitian lanjutan adalah notifikasi yang membuka tujuan ketika diketuk pengguna. Perangkat terkunci, interaksi pengguna, dan aturan PendingIntent perlu diuji pada alur tersebut.

### 11.4 Wi-Fi, waktu, dan pemicu lainnya

Pemicu di luar lokasi dan koneksi TWS belum dinilai lengkap pada dokumen v0.1. Jangan menurunkan penilaian dukungannya hanya dari kemampuan membaca nilai saat UI terbuka. Setiap pemicu baru perlu jalur event, akses, perilaku saat proses mati, dan cara memverifikasi hasil.

## 12. Repository dan sumber implementasi

| Repository | Hasil pemeriksaan | Penggunaan yang disarankan |
| --- | --- | --- |
| `android/platform-samples` | Repository resmi kumpulan API platform yang tersedia saat pemeriksaan. | Titik awal mencari contoh API dan lokasi. Cocokkan contoh dengan dokumentasi saat ini. [S31] |
| `android/nowinandroid` | Repository resmi aplikasi Kotlin dan Compose. | Referensi struktur aplikasi; tidak dianggap implementasi otomasi perangkat siap pakai. [S32] |
| `android/location-samples` | Diarsipkan pemilik pada 23 Agustus 2023; README mengarahkan contoh lokasi baru ke platform-samples. | Bahan sejarah dan pembanding. Hindari menyalin konfigurasi lama sebagai acuan mutakhir. [S33] |

Saat mulai implementasi, catat commit contoh yang benar-benar digunakan, SDK yang dipakai, versi dependensi, dan tanggal verifikasi. PRD serta dokumen ini tidak mengunci versi library berdasarkan perkiraan.

Repository resmi juga dapat berisi contoh yang lebih sempit dari kebutuhan Krono. Contoh scan BLE tidak membuktikan pemicu TWS, dan contoh geofence tidak membuktikan pemilihan seluruh cabang aturan Krono.

## 13. Masukan kemampuan untuk mesin aturan

Tahap `RULE_ENGINE.md` perlu menerima hasil pemeriksaan kemampuan sebelum memilih aksi. Bentuk akhirnya belum ditentukan; kebutuhan informasinya sebagai berikut.

| Informasi | Kegunaan |
| --- | --- |
| API runtime dan target aplikasi | Menentukan cabang perilaku platform yang berlaku. |
| Status akses per fitur | Membedakan akses diberikan, ditolak, dan perlu pengaturan. |
| Ketersediaan sumber | Mengetahui apakah lokasi atau keadaan Bluetooth dapat dibaca. |
| Waktu dan asal pengamatan | Membedakan kejadian baru, keadaan lama, simulasi, dan rekonsiliasi. |
| Kelayakan konteks eksekusi | Menghindari menjalankan aksi pada keadaan yang tidak sesuai; keberadaan FGS saja belum cukup menjadi bukti untuk semua keadaan. |
| Aset dan sasaran aksi | Memastikan gambar dan sasaran perubahan masih tersedia. |
| Hasil pemeriksaan pascaaksi | Memisahkan permintaan dari hasil yang bisa diamati. |
| Kebutuhan tindakan pengguna | Menentukan pesan dan langkah lanjut tanpa mengubah logika ELSE. |

Keputusan desain harus mempertahankan ketidakpastian. Bila keadaan lifecycle atau sumber belum dapat dibuktikan, engine perlu hasil yang dapat ditunda atau dilewati, bukan mengarang status siap.

## 14. Dampak penelitian terhadap PRD v0.1

Bagian ini mencatat penajaman yang perlu dibawa ke dokumen berikutnya. File PRD tidak diubah dalam tahap ini.

| Kebutuhan PRD | Penajaman dari penelitian | Konsekuensi rancangan |
| --- | --- | --- |
| FR-06, FR-20 | Registrasi geofence merupakan keadaan yang berbeda dari konfigurasi tersimpan. | Simpan kebutuhan pemantauan dan lakukan rekonsiliasi registrasi. |
| FR-07 | Classic dan LE Audio mempunyai jalur event yang berbeda. | Jangan mengklaim dukungan semua TWS dari satu jenis uji. |
| FR-09 | Perubahan volume background Android 17 memerlukan pemeriksaan lifecycle yang lebih ketat. | Tambahkan pemeriksaan kelayakan per aksi dan PoC sesi audio. |
| FR-08 | Kemampuan memasang wallpaper berbeda dari membaca gambar wallpaper lama. | Pertahankan pilihan gambar cadangan eksplisit; jangan mengandalkan snapshot wallpaper sistem. |
| FR-12, FR-13 | Kesiapan bersifat per kemampuan dan keadaan sumber dapat tidak diketahui. | UI menjelaskan hambatan yang tepat tanpa menjalankan ELSE berdasarkan asumsi. |
| FR-16 | Tidak adanya exception belum membuktikan volume berubah. | Tambahkan hasil Belum terverifikasi atau padanannya pada rancangan riwayat. |
| FR-17, FR-19 | Event profil atau pemulihan dapat muncul berulang. | Normalisasi dan pencegahan aksi ganda tetap diperlukan. |
| FR-20 | Pemulihan pemantauan bukan pemulihan otomatis kelayakan sesi audio. | Mungkin perlu pengguna melanjutkan sesi setelah gangguan tertentu. |

Temuan Android 17 tidak membatalkan tujuan TWS → volume. Temuan ini membuat janji otomasi penuh harus mengikuti jalur yang benar-benar terbukti. Jika jalur utama tidak memenuhi syarat, alternatif dengan interaksi pengguna ditawarkan sebagai mode yang berbeda dan dijelaskan secara jelas.

## 15. Matriks verifikasi perangkat

Semua baris di bawah **Belum dijalankan**. Ini daftar eksperimen yang diperlukan, bukan laporan hasil. Simulator dapat membantu memeriksa logika, sedangkan dukungan aksesori dan background perlu perangkat yang sesuai.

| ID | Sasaran uji | Hasil yang harus dibuktikan | Status |
| --- | --- | --- | --- |
| V-01 | Android 12+; akses Bluetooth saja | Aturan TWS dapat dikonfigurasi tanpa memaksa akses lokasi. | Belum dijalankan |
| V-02 | TWS Classic; proses Krono tidak hidup | Kejadian profil yang relevan diterima melalui jalur yang dipilih. | Belum dijalankan |
| V-03 | TWS lain dengan nama sama | Pemilihan perangkat tidak bergantung hanya pada nama. | Belum dijalankan |
| V-04 | A2DP dan headset panggilan melaporkan koneksi | Satu perubahan penggunaan tidak menghasilkan aksi ganda. | Belum dijalankan |
| V-05 | TWS LE Audio | Kedua earbud dan perpindahan transport dinormalisasi sesuai model. | Belum dijalankan |
| V-06 | Pengujian volume dengan UI terlihat | Target dipetakan ke rentang sistem dan hasil yang tersedia diperiksa. | Belum dijalankan |
| V-07 | Android 17; volume dari receiver biasa | Ketika dibatasi, tidak ada status berhasil palsu. | Belum dijalankan |
| V-08 | Android 17; target 37; FGS tanpa WIU | Hambatan eksekusi dapat diamati dalam diagnostik dan ditangani. | Belum dijalankan |
| V-09 | Android 17; sesi audio dimulai pengguna | Jenis layanan dan jalur lifecycle yang dipilih benar-benar dapat menjalankan skenario. | Belum dijalankan |
| V-10 | Sesi audio setelah boot atau proses mati | Pemantauan dan kemampuan aksi dipulihkan atau dijelaskan secara terpisah. | Belum dijalankan |
| V-11 | Dua perangkat audio terhubung | Dampak volume dan batas sasaran per perangkat diketahui. | Belum dijalankan |
| V-12 | Nilai volume sudah sesuai target | Hasil Tidak perlu perubahan dicatat tanpa klaim perubahan baru. | Belum dijalankan |
| V-13 | Pengguna mengubah volume setelah aksi | Nilai manual tidak terus ditimpa. | Belum dijalankan |
| V-14 | Lokasi presisi ditolak, perkiraan diberikan | Kesiapan geofence tidak disamakan dengan akses presisi penuh. | Belum dijalankan |
| V-15 | Background location ditolak atau dicabut | Aturan lokasi menjelaskan hambatan; fitur lain tetap tersedia. | Belum dijalankan |
| V-16 | Masuk A lalu B dalam satu kawasan besar | Kedua area dipantau dan perubahan cabang relevan dapat terjadi. | Belum dijalankan |
| V-17 | Area tumpang tindih dan lokasi belum diketahui | Model input tetap konsisten dengan urutan cabang dan aturan unknown. | Belum dijalankan |
| V-18 | Registrasi mendekati batas atau layanan lokasi hilang | Registrasi gagal ditampilkan sebagai masalah pemantauan. | Belum dijalankan |
| V-19 | Restart dan pembaruan aplikasi | Registrasi yang diperlukan direkonsiliasi tanpa mengulang aksi secara buta. | Belum dijalankan |
| V-20 | Force Stop pada Android 15+ | Pemantauan tidak dijanjikan berjalan; pemulihan diperiksa setelah pengguna kembali. | Belum dijalankan |
| V-21 | Wallpaper layar utama, kunci, dan keduanya | Sasaran serta hasil visual sesuai dukungan perangkat. | Belum dijalankan |
| V-22 | Gambar dipilih lalu perangkat restart | Sumber tetap dapat dibaca melalui mekanisme yang dipilih. | Belum dijalankan |
| V-23 | Sumber gambar hilang atau gagal dibaca | Aksi gagal secara jelas dan urutan berikutnya mengikuti kebijakan. | Belum dijalankan |
| V-24 | Doze, layar terkunci, dan pembatasan vendor | Waktu dan batas penerimaan serta eksekusi dicatat per perangkat. | Belum dijalankan |
| V-25 | Izin notifikasi ditolak | Keadaan notifikasi dipisahkan dari izin fitur lain dan kelayakan FGS. | Belum dijalankan |

Catatan eksperimen harus memuat model HP, versi Android dan build, target SDK aplikasi, versi aplikasi, jenis/profil TWS, akses yang diberikan, kondisi layar, keadaan proses/layanan, waktu kejadian, dan hasil yang diamati. Identitas perangkat atau koordinat lengkap hanya digunakan bila benar-benar diperlukan untuk diagnosis lokal.

## 16. Urutan pembuktian yang disarankan

1. **Uji aksi dasar ketika UI terlihat.** Buktikan pemasangan wallpaper dan pemetaan volume sebelum melibatkan pemicu background.
2. **Buktikan jalur audio Android 17.** Ini ketidakpastian penting. Uji konteks yang diizinkan dan yang dibatasi, dengan jenis layanan yang sesuai pekerjaan nyata.
3. **Buktikan pemicu TWS Classic.** Uji pengiriman ketika proses tidak hidup, normalisasi event, serta hubungan pemicu dengan jalur audio yang sudah diperiksa.
4. **Buktikan geofence A/B.** Uji akses bertahap, sumber tidak tersedia, perpindahan internal, dan pemulihan registrasi.
5. **Buktikan penggabungan pemicu dan aksi.** Pastikan pekerjaan yang tertunda tidak memakai keadaan kedaluwarsa atau menimpa perubahan manual tanpa alasan.
6. **Perluas matriks perangkat.** Tambahkan LE Audio, beberapa keluaran, dan variasi vendor setelah jalur awal jelas.

Selama pembuktian, `dumpsys audio` dan logcat dapat membantu menemukan penolakan audio; dokumentasi menyebut penanda diagnostik `AudioHardening`. Gunakan hasilnya untuk pengujian, bukan sebagai API bisnis aplikasi. [S04]

Dokumen ini tidak mensyaratkan menonaktifkan pembatasan sistem pada HP pengguna. Hasil uji dengan pembatasan dinonaktifkan tidak dihitung sebagai bukti dukungan konfigurasi normal.

## 17. Keputusan yang masih terbuka

| ID | Keputusan | Dasar yang masih diperlukan |
| --- | --- | --- |
| OPEN-01 | Minimum Android dan target build final | Hasil PoC, ketersediaan toolchain, dan sasaran distribusi. |
| OPEN-02 | Jenis FGS dan bentuk sesi otomasi audio | Kecocokan penggunaan serta hasil Android 17. |
| OPEN-03 | Perilaku melanjutkan sesi setelah gangguan | Kelayakan lifecycle dan pengalaman pengguna yang diuji. |
| OPEN-04 | Cakupan TWS Classic, LE Audio, dan konfigurasi beberapa keluaran | Bukti perprofil dan perangkat. |
| OPEN-05 | Provider lokasi dan perilaku tanpa Google Play services | Jalur alternatif yang benar-benar diteliti dan diuji. |
| OPEN-06 | Pemilihan peta dan sumber titik lokasi | Keputusan pengalaman pengguna serta ketergantungan penyedia. |
| OPEN-07 | Salinan gambar internal atau URI persisten | Pengelolaan aset, ketahanan sumber, dan kebutuhan penyimpanan. |
| OPEN-08 | Umur data kondisi dan kedaluwarsa aksi | Kontrak mesin aturan serta karakter sumber. |
| OPEN-09 | Istilah hasil belum terverifikasi | Hubungan dengan desain riwayat dan kriteria keberhasilan. |

Ketidakpastian yang tercatat tidak menahan penyusunan model mesin aturan. Model tersebut dapat disiapkan dengan masukan kemampuan dan hasil unknown, sementara dukungan per perangkat menunggu pembuktian.

## 18. Keluaran tahap ini dan tahap berikutnya

Tahap ini menghasilkan pemetaan kemampuan, izin, batas versi, kebutuhan background, dan daftar pembuktian untuk dua alur inti Krono. Hasilnya berbasis penelitian; status pengujian perangkat belum berubah.

Pekerjaan berikutnya adalah `RULE_ENGINE.md`. Dokumen tersebut perlu menentukan semantik kondisi, pemilihan cabang, normalisasi event, umur keadaan, prioritas, aksi tertunda, pembatalan, pengulangan, dan pemulihan dengan memasukkan batas kemampuan pada dokumen ini.

`ARCHITECTURE.md` kemudian memilih komponen serta jalur eksekusi. Pilihan tersebut harus menjelaskan mengapa setiap aksi memiliki konteks yang diizinkan. Mengganti bahasa atau framework UI saja tidak menghilangkan batas Android.

## 19. Referensi resmi

Semua tautan pada daftar ini diperiksa pada 22 Oktober 2026. Tanggal pemeriksaan berbeda dari tanggal pembaruan sumber. Dokumen audio Android 17 yang relevan mencantumkan pembaruan 16 Oktober 2026. Halaman API dapat memuat tambahan versi baru; periksa penanda versi dan ketersediaan saat implementasi.

| ID | Sumber | Penggunaan |
| --- | --- | --- |
| [S01] | Android 17 overview | Konteks platform dan pemisahan perubahan perilaku. |
| [S02] | Android 17: behavior changes for all apps | Dampak runtime, termasuk audio background. |
| [S03] | Android 17: behavior changes for apps targeting 17 | Persyaratan tambahan menurut target SDK. |
| [S04] | Android 17: background audio hardening | Konteks audio yang diizinkan, kegagalan senyap, dan diagnosis. |
| [S05] | Create and monitor geofences | Model area, kapasitas, ketepatan, jeda, dan kebutuhan pemantauan. |
| [S06] | Google Play services: GeofencingClient | Registrasi, izin, mutabilitas PendingIntent, dan penghapusan. |
| [S07] | Request location access at runtime | Alur akses lokasi dan pilihan ketepatan. |
| [S08] | Request background location | Perbedaan dialog, pengaturan, dan pilihan akses background. |
| [S09] | Bluetooth permissions | Izin modern, kompatibilitas, serta penggunaan scan. |
| [S10] | BluetoothA2dp | Kejadian perubahan koneksi profil A2DP. |
| [S11] | BluetoothLeAudio | Peristiwa LE Audio dan keadaan kedua sisi earbud. |
| [S12] | Bluetooth Low Energy Audio overview | Dukungan platform dan transport LE Audio. |
| [S13] | Implicit broadcast exceptions | Pengecualian kejadian Bluetooth serta boot. |
| [S14] | Broadcasts overview | Penerimaan serta konfigurasi broadcast. |
| [S15] | BroadcastReceiver | Umur penerimaan dan batas `goAsync()`. |
| [S16] | AudioManager | Jenis volume, rentang, dan operasi perubahan. |
| [S17] | Manifest.permission | Jenis izin normal wallpaper dan audio. |
| [S18] | WallpaperManager | Pemasangan, dukungan, sasaran layar, serta batas pembacaan. |
| [S19] | Photo Picker | Pemilihan gambar dan akses yang dipertahankan. |
| [S20] | WorkManager: define work requests | Pekerjaan periodik, penjadwalan, dan kuota. |
| [S21] | Restrictions on starting a foreground service from the background | Batas peluncuran dan hubungan dengan akses WIU. |
| [S22] | Foreground service types | Kecocokan jenis layanan dan prasyaratnya. |
| [S23] | Notification runtime permission | Perbedaan izin notifikasi dan memulai FGS. |
| [S24] | Android 15: behavior changes for all apps | Force Stop, stopped state, dan PendingIntent. |
| [S25] | Optimize for Doze and App Standby | Penundaan aktivitas background. |
| [S26] | Companion device pairing | Asosiasi dan jalur akses background terkait perangkat. |
| [S27] | NotificationManager | Akses kebijakan notifikasi dan perilaku DND. |
| [S28] | AOSP main: core AndroidManifest.xml | Tingkat perlindungan izin daya internal. |
| [S29] | AOSP main: PowerManagerService.java | Pemeriksaan akses pada perubahan hemat daya. |
| [S30] | Activity security | Pembatasan peluncuran layar dari background. |
| [S31] | Repository android/platform-samples | Contoh resmi API platform yang diperiksa. |
| [S32] | Repository android/nowinandroid | Referensi struktur aplikasi Kotlin dan Compose. |
| [S33] | Repository android/location-samples | Status arsip dan pengalihan contoh lokasi. |

[S01]: https://developer.android.com/about/versions/17
[S02]: https://developer.android.com/about/versions/17/behavior-changes-all
[S03]: https://developer.android.com/about/versions/17/behavior-changes-17
[S04]: https://developer.android.com/about/versions/17/changes/bg-audio
[S05]: https://developer.android.com/develop/sensors-and-location/location/geofencing
[S06]: https://developers.google.com/android/reference/com/google/android/gms/location/GeofencingClient
[S07]: https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
[S08]: https://developer.android.com/develop/sensors-and-location/location/permissions/background
[S09]: https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
[S10]: https://developer.android.com/reference/android/bluetooth/BluetoothA2dp
[S11]: https://developer.android.com/reference/android/bluetooth/BluetoothLeAudio
[S12]: https://developer.android.com/develop/connectivity/bluetooth/ble-audio/overview
[S13]: https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions
[S14]: https://developer.android.com/develop/background-work/background-tasks/broadcasts
[S15]: https://developer.android.com/reference/android/content/BroadcastReceiver
[S16]: https://developer.android.com/reference/android/media/AudioManager
[S17]: https://developer.android.com/reference/android/Manifest.permission
[S18]: https://developer.android.com/reference/android/app/WallpaperManager
[S19]: https://developer.android.com/training/data-storage/shared/photo-picker
[S20]: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work
[S21]: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
[S22]: https://developer.android.com/develop/background-work/services/fgs/service-types
[S23]: https://developer.android.com/develop/ui/compose/notifications/notification-permission
[S24]: https://developer.android.com/about/versions/15/behavior-changes-all
[S25]: https://developer.android.com/training/monitoring-device-state/doze-standby
[S26]: https://developer.android.com/develop/connectivity/bluetooth/companion-device-pairing
[S27]: https://developer.android.com/reference/android/app/NotificationManager
[S28]: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/res/AndroidManifest.xml
[S29]: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/services/core/java/com/android/server/power/PowerManagerService.java
[S30]: https://developer.android.com/guide/components/activities/secure-bal
[S31]: https://github.com/android/platform-samples
[S32]: https://github.com/android/nowinandroid
[S33]: https://github.com/android/location-samples

## 20. Riwayat dokumen

| Versi | Tanggal | Perubahan |
| --- | --- | --- |
| 0.1 | 22 Oktober 2026 | Pemetaan awal kemampuan Android berdasarkan PRD Krono, sumber resmi terkini yang diperiksa, dan daftar verifikasi perangkat. |

