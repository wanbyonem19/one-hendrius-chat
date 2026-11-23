# ONE HENDRIUS CHAT — APK + Link (Firebase gratis)

Satu codebase, satu backend Firebase gratis, dua wujud:
- **APK Android**: `one-hendrius-chat.apk` (package `com.bahlil.onehendrius`)
- **Link publik**: deploy folder `web/` ke Firebase Hosting (gratis) atau GitHub Pages

## File penting
- `web/index.html` — aplikasi web single-page (CANONICAL). Isi `FIREBASE_CONFIG` di sini.
- `assets/app/index.html` — salinan yang dibundel APK (dibuat otomatis oleh `build.sh`).
- `build.sh` — build APK (aapt2 → javac → d8 → zipalign → apksigner).
- `firebase.json` — config deploy Firebase Hosting (public: `web`).
- `firestore.rules` / `storage.rules` — Rules untuk ditempel di Firebase Console.
- `PANDUAN_FIREBASE.txt` — langkah lengkap: buat proyek Firebase, isi config,
  deploy link, build ulang APK. Baca ini dulu.
- `app.keystore` — JANGAN hilang; update APK harus pakai keystore yang sama.

## Status
- APK v1.0 ter-build + ter-sign, WebView memuat `index.html` lokal.
- Config Firebase masih PLACEHOLDER — aplikasi menampilkan layar panduan
  setup sampai `FIREBASE_CONFIG` diisi (tidak crash).
- Belum bisa dites end-to-end (login/kirim pesan) sebelum ada proyek Firebase asli.
