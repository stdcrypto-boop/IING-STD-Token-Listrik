# Standar keamanan dan distribusi APK IING STD

- Pertahankan applicationId/package name, signing certificate, dan versionCode yang meningkat untuk update tanpa uninstall.
- Jangan pernah commit keystore (.jks/.keystore), password, token, atau isi secret. Simpan hanya pada GitHub Actions Secrets dengan izin minimum.
- APK debug yang ditandatangani kunci sementara **bukan** rilis stabil dan **tidak dijamin** dapat memperbarui APK sebelumnya. Jangan mempromosikannya sebagai update.
- APK siap pakai harus lolos build, verifikasi apksigner, dan pengujian relevan sebelum diterbitkan di GitHub Releases sebagai file .apk langsung.
- Gunakan kunci berbeda untuk aplikasi baru bila memungkinkan. Untuk aplikasi lama, identifikasi sertifikat APK yang terpasang sebelum memutuskan kunci penandatanganan.
- Gunakan tag rilis unik, SHA-256, dan permissions: contents: write hanya pada job yang perlu membuat GitHub Release.
- Hindari logging secrets, serta jangan mengunggah keystore sebagai artifact.
- Saat belum ada signing key stabil, tahan publikasi sebagai APK pembaruan; jangan diam-diam mengganti sertifikat.
- Terapkan pedoman ini ke proyek baru saat pembuatan workflow pertamanya.

Status: dokumen ini adalah standar; keberadaan dokumen **tidak** berarti workflow setiap proyek telah memakai signing key permanen.
