# Driver Trip dan Location Implementation Plan

## Ringkasan

Akun driver, first-login, penggantian password, dan pemulihan session sudah selesai. Tahap berikutnya dimulai dari assigned trip, dilanjutkan dengan validasi secure RPC, foreground GPS, location outbox, dan Realtime untuk passenger.

## 1. Aturan perubahan database

| Perubahan | Lokasi |
| --- | --- |
| Table, column, enum, constraint | Migration |
| RPC/function, trigger, RLS policy | Migration |
| Index dan privilege | Migration |
| Route catalogue deterministik | `supabase/seed.sql` atau migration data terkontrol |
| Driver account | Provisioning script |
| Vehicle dan trip assignment development | SQL Editor atau script operasional |
| Service-role key | Environment variable, tidak boleh masuk Git |

Jangan mengedit migration yang sudah pernah di-push. Semua koreksi dibuat melalui migration additive baru. Perubahan schema langsung melalui SQL Editor dapat menyebabkan schema remote dan migration history tidak sinkron.

## 2. Workflow migration di codebase

Periksa status migration sebelum membuat perubahan:

```powershell
npx supabase migration list --remote
```

Buat migration baru dengan nama yang menjelaskan perubahan:

```powershell
npx supabase migration new add_driver_location_rpc
```

Masukkan SQL ke file yang dibuat di:

```text
supabase/migrations/<timestamp>_add_driver_location_rpc.sql
```

Jika Docker tersedia, uji migration dari kondisi database kosong:

```powershell
npx supabase start
npx supabase db reset
npx supabase test db
```

Jika menggunakan hosted development project tanpa Docker:

```powershell
npx supabase db push --dry-run --remote
npx supabase db push --remote
npx supabase db lint --remote
npx supabase migration list --remote
```

Commit migration secara terpisah setelah berhasil:

```powershell
git add supabase/migrations/<nama-file-migration>.sql
git commit -m "feat(database): add driver location RPC"
```

Hanya satu anggota tim yang menjalankan `db push` pada satu waktu.

### Jika telanjur mengubah schema di SQL Editor

1. Hentikan perubahan schema berikutnya di dashboard.
2. Buat diff antara remote dan migration lokal:

   ```powershell
   npx supabase db diff --remote -f capture_remote_schema_change
   ```

3. Periksa migration yang dihasilkan secara manual.
4. Pastikan file hanya berisi perubahan yang memang dilakukan.
5. Jangan menjalankan `migration repair` sebelum memahami perbedaan schema dan migration history.

## 3. Buat assigned trip development

Assigned trip tidak dibuat sebagai migration karena menggunakan UUID akun khusus `shuttlespot-dev` dan merupakan data operasional.

Gunakan Supabase SQL Editor atau script development terpisah dengan:

- Driver ID: `0683adfa-ca33-47f0-88fc-d4ed853c4ef3`
- Route ID: `10000000-0000-0000-0000-000000000001`
- Vehicle: `Shuttle Dev 01`
- Capacity: `20`
- Trip status: `not_started`
- Current stop sequence: `0`

Sebelum insert:

- pastikan profile driver tersedia dan aktif;
- pastikan driver tidak mempunyai trip `not_started` atau `active`;
- gunakan vehicle aktif;
- jangan memasukkan UUID driver ke `seed.sql` atau migration.

Checkpoint:

- assigned trip muncul pada dashboard;
- driver tanpa assignment tetap melihat empty state;
- route, vehicle, jumlah halte, dan demand tampil benar.

## 4. Validasi secure trip RPC

Uji alur berikut dari aplikasi:

1. Muat assignment dengan `get_assigned_trip()`.
2. Mulai trip dengan `start_assigned_trip()`.
3. Pastikan status menjadi `active` dan `started_at` terisi.
4. Majukan halte dengan `advance_trip_stop()`.
5. Tampilkan demand dari `get_stop_demand()`.
6. Selesaikan trip dengan `finish_assigned_trip()`.
7. Pastikan status menjadi `completed` dan `ended_at` terisi.

Validasi:

- double-tap tidak mengirim aksi kedua;
- sequence hanya bertambah satu;
- trip tidak selesai sebelum halte terakhir;
- driver tidak dapat mengendalikan trip driver lain;
- passenger tidak dapat memanggil RPC driver;
- completed trip tidak dapat dimulai kembali;
- direct update ke `trips` tetap ditolak.

`LocationPublisher` masih no-op pada tahap ini. Aplikasi belum boleh menyatakan bahwa lokasi sedang dibagikan.

## 5. Tutup milestone secure driver trip

Milestone selesai ketika:

- assigned trip tampil;
- start, advance, demand, dan finish bekerja;
- loading, no-assignment, error, retry, active, dan completed state benar;
- repository, ViewModel, dan navigation/UI tests lulus;
- Gradle checks lulus.

Jalankan:

```powershell
.\gradlew testDebugUnitTest
.\gradlew assembleDebug
.\gradlew lintDebug
```

Perbarui:

- `docs/architecture.md`
- `docs/product.md`
- `docs/roadmap.md`

## 6. Implementasikan foreground GPS

Buat migration:

```powershell
npx supabase migration new add_publish_vehicle_location_rpc
```

Migration harus menambahkan `publish_vehicle_location` yang:

- mengambil driver dari `auth.uid()`;
- hanya menerima trip aktif milik driver tersebut;
- memvalidasi latitude, longitude, accuracy, dan timestamp;
- memasukkan lokasi secara atomik;
- tidak menerima `driver_id` dari Android;
- hanya memberikan `EXECUTE` kepada role yang diperlukan.

Pada Android:

- gunakan Fused Location Provider;
- minta foreground-location permission saat trip dimulai;
- jalankan foreground service hanya selama trip aktif;
- kirim lokasi setelah pergerakan bermakna atau interval 10–15 detik;
- hentikan service saat trip selesai atau logout;
- tampilkan state permission denied dan location disabled;
- jangan meminta background-location permission.

## 7. Tambahkan location outbox

- Simpan update lokasi yang gagal ke Room.
- Batasi jumlah record location outbox.
- Gunakan idempotency key.
- Retry melalui WorkManager ketika online.
- Pertahankan titik terbaru dan buang titik lama yang tidak lagi berguna.
- Jalankan cleanup server untuk lokasi berusia lebih dari 24 jam.

Checkpoint:

- lokasi pulih setelah offline-to-online;
- retry tidak menghasilkan duplikasi;
- finish trip menghentikan pengambilan dan pengiriman lokasi.

## 8. Hubungkan passenger melalui Realtime

- Subscribe hanya ke active trip pada route passenger.
- Terima perubahan trip dan lokasi terbaru.
- Simpan snapshot terakhir ke Room.
- Tampilkan timestamp pembaruan.
- Bedakan state fresh, stale, offline, dan completed.
- Perbarui progress dan ETA dari posisi serta halte terakhir.

Milestone selesai ketika update GPS driver terlihat pada tracker passenger dan penyelesaian trip menghentikan live tracking dengan state yang jujur.
