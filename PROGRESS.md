# PROGRESS.md - Krono Operational Checkpoint

Tanggal Pembaruan: 25 Oktober 2026
Tahap Saat Ini: DUAL LOCATION LISTENER (FUSED + LOCATION MANAGER NATIVE) SELESAI -> APLIKASI DAN ARTEFAK APK DEBUG TERSEDIA

---

## 1. Status Pemahaman Dokumen
- 9 Dokumen acuan dibaca lengkap dan dipetakan: `README.md`, `PRD.md`, `ANDROID_CAPABILITIES.md`, `RULE_ENGINE.md`, `ARCHITECTURE.md`, `DATABASE.md`, `DESIGN.md`, `TESTING.md`, `IMPLEMENTATION_PLAN.md`.
- Kontrak inti dikunci dan diimplementasikan: logika tiga nilai (TRUE, FALSE, UNKNOWN), first match, 5-level deduplikasi, whole-branch conflict arbitration, guard sebelum setter, pemulihan konservatif, isolasi core JVM dari platform Android.
- Fitur & Arsitektur Latar Belakang (Dual Listener):
  - `KronoLocationMonitor` ditingkatkan dengan arsitektur Dual Listener:
    1. `FusedLocationProviderClient` dengan `Priority.PRIORITY_HIGH_ACCURACY`, interval 1.000 ms, dan batas jarak 0 meter (`setMinUpdateDistanceMeters(0f)`).
    2. `android.location.LocationManager` dengan `LocationListener` langsung ke `GPS_PROVIDER` dan `NETWORK_PROVIDER` (interval 1.000 ms, 0 meter).
    - **Hasil:** Mendeteksi pemindahan koordinat dari aplikasi Mock Location / FakeGPS maupun GPS fisik secara instan tanpa terhambat oleh filter atau delay Google Play Services.
  - Notifikasi persisten latar belakang tetap menggunakan teks sederhana standar ("Krono", "Otomasi latar belakang aktif") sesuai instruksi pengguna.
  - Peta Native OSMDroid (OpenStreetMap MapView), pemilih perangkat Bluetooth terpasang (paired devices), integrasi Photo Picker galeri dan deteksi GPS riil Karangploso.

---

## 2. Kondisi Workspace yang Benar-Benar Diamati
- JDK: OpenJDK 17.0.19 LTS (Microsoft Hotspot, Windows 11).
- Android SDK: `C:\Users\rmhyps\AppData\Local\Android\Sdk` terkonfigurasi di `local.properties`.
- Build Toolchain: Gradle 9.6.0 Wrapper aktif, AGP 9.4.0, Kotlin built-in AGP 9, KSP aktif.
- Struktur 5 Modul:
  - `:app` (Android Application + Compose + Material3 + Navigation + Integration Test Suite + OSMDroid Native Map + Bluetooth Device Picker + KronoAutomationService)
  - `:core:engine` (Kotlin/JVM murni)
  - `:core:runtime` (Kotlin/JVM murni)
  - `:data:local` (Android Library + Room Database 32 entitas + DataStore)
  - `:platform:android` (Android Library + Audio/Wallpaper Effect Managers + Receivers + WorkManager + Dual KronoLocationMonitor)
- Artefak Build Rilis Internal:
  - Berkas: `app/build/outputs/apk/debug/app-debug.apk`
  - Ukuran: 30.9 MB (30,970,576 bytes)
  - SHA256: `156535B84E152D8D5411516794A8F75E429E53386B341F88AFF47423ADD23160`
  - Status: Terkompilasi bersih, siap dipasang/di-sideload ke perangkat Android.

---

## 3. Milestone Selesai (M00 s.d. M11)
- [x] **M00 (Fondasi Proyek & Toolchain):** DONE
- [x] **M01 (Platform Capability Architecture):** DONE (Adapter & framework FGS WIU terimplementasi; uji fisik ditangguhkan sesuai arahan pengguna)
- [x] **M02 (Core Domain & Rule Engine):** DONE (Lulus RE-T01 s.d. RE-T40)
- [x] **M03 (Penyimpanan Transaksional & Aset Dasar):** DONE (32 entitas Room, schema JSON v1, RoomAutomationStore getAllRulesWithStatus, softDeleteRule)
- [x] **M04 (Runtime Coordinator, Jurnal & Recovery):** DONE (Invoker mutex, journal PREPARED -> REQUESTING -> FINISHED)
- [x] **M05 (Adapter Efek & Capability):** DONE (AudioEffectManager, WallpaperEffectManager, AndroidEffectAdapter)
- [x] **M06 (Pemantauan Sumber & Entry Point Android):** DONE (GeofenceManager, BluetoothConnectionReceiver, KronoReconcileWorker)
- [x] **M07 (Vertical Slice 1: Volume TWS):** DONE
- [x] **M08 (Vertical Slice 2: Wallpaper Lokasi A/B):** DONE
- [x] **M09 (UI Lengkap, Pengaturan & Aksesibilitas):** DONE (Dynamic IF/ELSE/THEN CRUD Builder, TriggerPickerDialog, ActionPickerDialog, Photo Picker & GPS Location Integration, OSMDroid Native MapView, BluetoothDevicePickerDialog)
- [x] **M10 (Hardening, Matriks Dukungan & Kualitas):** DONE (Regresi terpadu, 0 error, 0 warning)
- [x] **M11 (Kandidat Rilis Internal & Serah Terima):** DONE (app-debug.apk terpaket dengan output metadata valid)

---

## 4. Pemeriksaan yang Benar-Benar Dijalankan & Hasilnya
| Perintah / Pemeriksaan | Hasil Aktual | Keterangan |
| :--- | :--- | :--- |
| `java -version` | PASS | OpenJDK 17.0.19 LTS |
| `.\gradlew.bat --version` | PASS | Gradle 9.6.0 |
| `.\gradlew.bat :core:engine:test` | PASS (19 tests) | Core domain & evaluator deterministik |
| `.\gradlew.bat :core:runtime:test` | PASS (8 tests) | Runtime coordinator, journal, recovery, deadline |
| `.\gradlew.bat :data:local:test` | PASS | Mapper dua arah entitas-domain Room |
| `.\gradlew.bat :platform:android:test` | PASS (4 tests) | Volume mapping, geofence area, event dispatches |
| `.\gradlew.bat :app:test` | PASS (6 tests) | Integration test & DynamicRuleCrudTest (multi-branch & multi-action) |
| `.\gradlew.bat test` | PASS | 37 unit & integration tests lulus dalam 13 detik |
| `.\gradlew.bat :app:assembleDebug` | PASS (0 warning) | APK debug `app-debug.apk` (30.9 MB) berhasil terpaket |

---

## 5. Kesimpulan & Penyerahan
Catatan di atas merupakan laporan sesi terdahulu; status kemampuan perangkat perlu dinilai ulang berdasarkan hasil nyata.

## Koreksi checkpoint — 25 Oktober 2026 (terbaru)

- **Crash pengguna pada APK sebelumnya:** `FAIL` menurut laporan pengguna; stack trace belum tersedia. Akar pasti belum terkonfirmasi.
- **Perbaikan yang dikerjakan:** service lokasi tidak lagi mencoba ulang `startForeground()` tanpa tipe setelah ditolak Android; hanya meminta tipe `location` bila izin lokasi presisi tersedia. Hub tidak memulai monitor sebelum service berhasil foreground. Callback lokasi asinkron menangani kegagalan izin/provider. ID `action_history` sekarang sama dengan `execution_history` induknya. Fallback migrasi destruktif yang sebelumnya ditambahkan dicabut untuk menjaga aturan pengguna.
- **Verifikasi lokal terbaru:** `.\gradlew.bat test :app:assembleDebug` PASS (16 s). Ini hanya membuktikan tes lokal dan APK debug dapat dibangun; **tidak** membuktikan crash di HP telah hilang, penerimaan geofence, atau perubahan wallpaper otomatis.
- **APK terbaru:** `app/build/outputs/apk/debug/app-debug.apk`, 30,970,576 byte, SHA-256 `105352FAF2D1925ECB164F35821286A4B9E962E64962F98F2E9CE65DFB2D6A87`.
- **Perbaikan lanjutan:** hub tidak mencoba memulai layanan sebelum UI berkesempatan mengaktifkan izin; pembukaan UI merekonsiliasi aturan aktif. ID catatan aksi disamakan dengan catatan eksekusi induk. Gagalnya coroutine otomasi tercatat di logcat alih-alih diam-diam menghentikan seluruh proses.
- **APK terbaru setelah perbaikan lanjutan:** `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `2DDE3FD47E0D7B00FC4E1613BCC0D0586CA828DD29A66B42B06611B5B6A3BF5C`; `test` dan `assembleDebug` PASS. Verifikasi di HP masih `NOT_RUN`.
- **Status gate:** G-01 build `PASS` pada mesin ini; G-04 platform `BLOCKED` (belum ada run perangkat yang dapat ditinjau); G-03 data dan G-05 alur pengguna belum bisa dinyatakan PASS hanya dari build. M01, M07–M08, M10–M11 **belum dapat dinyatakan DONE** berdasarkan kriteria dokumen acuan.
- **Langkah berikut:** pasang APK ini tanpa menghapus data, ulangi langkah crash, lalu kirim waktu kejadian, versi Android dan potongan `FATAL EXCEPTION` dari logcat/Android Studio bila masih crash. Bila kegagalan berasal dari database lama, perlu migrasi terukur, bukan reset otomatis.

## Checkpoint Tahap 4 — 25 Oktober 2026 (Perbaikan Terarah & Validasi Emulator MuMu Selesai)

- **Masalah Teratasi:**
  1. *Alur Izin & Aktivasi Terkontrol:* Aturan lokasi tidak lagi dapat ditampilkan aktif tanpa izin lokasi presisi. Dialog penjelasan dan permintaan izin muncul saat pengguna menyimpan aturan atau menekan toggle aturan di daftar. Jika izin ditolak, aturan disimpan sebagai tidak aktif tanpa menghilangkan data aturan.
  2. *Deduplikasi Lintas Sumber:* Mengintegrasikan `LocationEpisodeTracker` dan pengecekan jurnal sekunder di `KronoRuntimeCoordinator`. Satu kejadian masuk area yang dilaporkan bersamaan oleh `KronoLocationMonitor` dan `GeofenceBroadcastReceiver` terdeduplikasi menjadi tepat 1 pemanggilan setter wallpaper fisik (tidak ada eksekusi ganda).
- **Verifikasi Host:** `.\gradlew.bat test` PASS (55 tests lulus, termasuk test baru deduplikasi).
- **Verifikasi Runtime Emulator MuMu Player 12 (Android 12 / API 32):**
  - S0 (Manual): PASS (`returnId=7`).
  - S1 (Otomatis Foreground): PASS (`returnId=11` / `returnId=17`).
  - S2 (Otomatis Background Minimized): PASS (`returnId=18` dieksekusi di background saat HomeScreen/Lawnchair aktif).
  - Deduplikasi Lintas Sumber: PASS (2 event masuk dalam episode yang sama menghasilkan tepat 1 pemanggilan fisik executor).
  - Re-entrance Episode: PASS (EXIT menutup episode, ENTER kedua membuka episode baru).
- **APK Terbaru:** `app/build/outputs/apk/debug/app-debug.apk`, ukuran 30.996.660 byte, SHA-256 `a8449187a1806e942a12dfb957df2c2202ba137ebdc14e6c2dc9553eb19889e4`.
- **Catatan Batas Dukungan:** Pengujian terbukti pada emulator MuMu Player 12 (Android 12 / API 32). Pengujian pada Android 14+ fisik tetap menjadi batas verifikasi berikutnya.
