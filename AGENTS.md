# AGENTS.md - Panduan Kerja AI Agent di Proyek Krono

Dokumen ini adalah pedoman wajib bagi AI Agent (Hermes, Claude, Cursor, dll.) saat bekerja di repository Krono Android.

---

## 1. Prinsip Utama: Plan-First & Perintah Eksplisit
1. **Default Mode: PLAN / BRAINSTORMING:**
   - Jangan langsung modifikasi source code, install dependency, atau ubah konfigurasi saat user melempar ide atau melaporkan masalah baru.
   - Mulai dengan memahami maksud user, brainstorming opsi solusi, dan menyusun tahap-tahap pengerjaan secara jelas.
2. **Eksekusi Hanya Atas Perintah:**
   - Eksekusi modifikasi file HANYA dijalankan setelah user memberikan perintah eksplisit (contoh: *"gas"*, *"eksekusi"*, *"lanjut"*).
3. **Pengerjaan Bertahap (Step-by-Step):**
   - Kerjakan satu tahap saja dalam satu waktu.
   - Setelah satu tahap selesai, laporkan hasilnya secara ringkas dan **BERHENTI**. Jangan otomatis loncat mengerjakan tahap berikutnya tanpa instruksi user.

---

## 2. Komunikasi & Klarifikasi
1. **Gaya Bahasa Santai:**
   - Gunakan bahasa Indonesia santai (gua/lu), kasual, to-the-point, dan jangan kaku/baku.
2. **Klarifikasi Sebelum Asumsi:**
   - Kalau ada hal yang ambigu, punya beberapa opsi pendekatan teknis, atau butuh keputusan arsitektur, **tanya ke user dulu**. Jangan berasumsi sendiri.

---

## 3. Disiplin Git & Checkpoint
1. **Dilarang Auto-Commit / Auto-Push:**
   - Setiap kali selesai mengedit atau memperbaiki kode, **JANGAN LANGSUNG dimasukkan ke Git (jangan auto-commit / auto-push)**.
   - Biarkan perubahan tetap berada di working tree supaya user bisa review diff-nya dulu.
   - Git commit dan push HANYA dilakukan jika user memberikan perintah spesifik (misal: *"commit ini"*, *"push ke github"*).
2. **Cadangan Lokal (Checkpoint):**
   - Untuk perubahan non-trivial atau perbaikan berisiko, buat salinan lokal file yang akan diubah ke folder temporary/checkpoint terlebih dahulu.

---

## 4. Standar Arsitektur Krono Android
1. **Struktur 5 Modul:**
   - `:app` -> UI Compose, Material3, Navigation, Foreground Service (`KronoAutomationService`), Composition Root / Hub.
   - `:core:engine` -> Pure Kotlin/JVM. Model aturan, evaluator 3-value logic (TRUE, FALSE, UNKNOWN), validator, arbitrase konflik. DILARANG mengimpor library Android atau Room.
   - `:core:runtime` -> Pure Kotlin/JVM. Koordinator runtime, execution plan, recovery, effect journal, episode tracker. DILARANG mengimpor library Android.
   - `:data:local` -> Android Library. Database Room 32 entitas, DataStore, AssetRepository (penyimpanan privat wallpaper `.bin`).
   - `:platform:android` -> Android Library. Listener lokasi (Fused & Native), GeofencingClient, WallpaperEffectManager, AudioEffectManager.
2. **Kerapian Kode:**
   - Jangan melakukan drive-by refactor atau merombak arsitektur di luar lingkup tugas yang diminta.
   - Selalu verifikasi hasil perubahan dengan `./gradlew.bat test` dan `./gradlew.bat :app:assembleDebug`.

---

## 5. Standar Bukti (Evidence-Based)
1. **Pisahkan Kategori Bukti:**
   - *STATIC_PROVEN*: Dibuktikan langsung dari pembacaan kode atau konfigurasi manifest.
   - *HOST_TEST*: Dibuktikan dari hasil test unit/integrasi Gradle di lingkungan host JVM.
   - *RUNTIME_PROVEN*: Dibuktikan dari observasi logcat / eksekusi nyata pada emulator (MuMu Player) atau perangkat fisik.
   - *HYPOTHESIS*: Dugaan yang masih butuh konfirmasi data log/run.
2. **Anti-Halusinasi:**
   - Jangan pernah mengarang isi logcat, status izin, port adb, atau hasil tes yang belum benar-benar dijalankan.
