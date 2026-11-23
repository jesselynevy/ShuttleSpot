# Implementasi GPS & Realtime Location (Fase 1 & Fase 2)

Dokumen ini merangkum file-file yang ditambahkan/diubah untuk fitur pelacakan bus
real-time: GPS driver dikirim ke cloud (Fase 1), lalu disampaikan ke penumpang secara
real-time (Fase 2). Termasuk status pemenuhan terhadap `DRIVER_TRIP_AND_LOCATION_PLAN.md`
dan daftar to-do yang belum dikerjakan.

Alur besar ujung-ke-ujung:

```
Driver (GPS)  ──insert──►  Supabase vehicle_locations  ──realtime──►  Passenger  ──write──►  Room  ──►  UI
   Fase 1                      (broker + RLS)                 Fase 2                    (reactive)
```

---

## Fase 1 — Driver mengirim GPS ke cloud

Tujuan: saat driver menjalankan trip aktif, posisi GPS-nya dimasukkan ke tabel
`vehicle_locations` setiap beberapa detik.

| File | Status | Kegunaan | Cara kerja singkat |
| --- | --- | --- | --- |
| `data/location/ShuttleSpotLocationService.kt` | Baru | Foreground service pengumpul GPS | Jalan selama trip aktif. Pakai Fused Location Provider, tiap interval ambil lokasi lalu panggil `DriverTripRepository.publishLocation()`. Menampilkan notifikasi persisten (syarat Android + transparansi). |
| `data/location/FusedLocationPublisher.kt` | Baru | Implementasi `LocationPublisher` | Adapter tipis: `start(tripId)`/`stop()` cuma menghidupkan/mematikan service di atas. ViewModel tidak tahu detail service. |
| `data/repository/DriverTripRepository.kt` | Ubah | Kontrak repository | Tambah method `publishLocation(tripId, lat, lng, accuracy, timestamp)` sebagai titik tunggal pengiriman lokasi. |
| `data/repository/InMemoryDriverTripRepository.kt` | Ubah | Adapter demo | `publishLocation` hanya log (tidak kirim ke mana-mana) saat `USE_DEMO_DATA=true`. |
| `data/remote/SupabaseDriverTripRepository.kt` | Ubah | Adapter produksi | `publishLocation` melakukan `postgrest["vehicle_locations"].insert(...)`. Gagal diam-diam (log) kalau RLS tolak/offline — satu sampel hilang tidak kritis. |
| `di/RepositoryModule.kt` | Ubah | Wiring Hilt | Binding `LocationPublisher` di-gate `USE_DEMO_DATA`: demo → `NoOpLocationPublisher`, produksi → `FusedLocationPublisher`. |
| `AndroidManifest.xml` | Ubah | Deklarasi service | `<service ... foregroundServiceType="location">`. Permission lokasi/FGS/notifikasi sudah ada sebelumnya. |
| `feature/driver/trip/DriverTripScreens.kt` | Ubah | Permission flow | Sebelum `startTrip()`, minta runtime permission lokasi + notifikasi. Hanya minta saat fitur dipakai. |
| `app/build.gradle.kts` + `gradle/libs.versions.toml` | Ubah | Dependency | Tambah `play-services-location` (Fused Location Provider). |
| `res/values/strings.xml` | Ubah | String notifikasi | Teks notifikasi service ("ShuttleSpot sedang berbagi lokasi bus", dll). |

Titik tempel lifecycle sudah ada di `DriverTripViewModel`: `startTrip()` memanggil
`locationPublisher.start(tripId)` dan `finishTrip()` memanggil `locationPublisher.stop()`.

---

## Fase 2 — Penumpang menerima GPS secara real-time

Tujuan: perangkat penumpang berlangganan (subscribe) perubahan di cloud, menulisnya ke
Room, sehingga UI (marker peta + status) ter-update otomatis tanpa refresh manual.

| File | Status | Kegunaan | Cara kerja singkat |
| --- | --- | --- | --- |
| `supabase/migrations/20261122182118_enable_realtime_for_location.sql` | Baru | Aktifkan realtime | `alter publication supabase_realtime add table public.trips, public.vehicle_locations` — mendaftarkan dua tabel agar perubahannya di-broadcast. |
| `data/remote/RemoteVehicleLocationDto.kt` | Baru | DTO payload realtime | Berisi `RemoteVehicleLocationDto` (lat/lng/captured_at) dan `RemoteTripStatusDto` (status trip). Payload JSON realtime di-deserialize ke sini. |
| `data/location/PassengerLocationStreamer.kt` | Baru | Orkestrator subscription | `@Singleton` yang menjaga lifecycle: session → primary selection → active trip → GPS feed. Dua channel (`trips` untuk status, `vehicle_locations` untuk GPS), lalu write-through ke Room. |
| `di/SupabaseModule.kt` | Ubah | Install plugin Realtime | Tambah `install(Realtime)` pada `createSupabaseClient`. |
| `data/local/ShuttleSpotDao.kt` | Ubah | Query one-shot | Tambah `suspend fun getTripSnapshot(routeId)` untuk membaca snapshot sekali (bukan Flow) saat menulis update lokasi. |
| `ShuttleSpotApplication.kt` | Ubah | Start orkestrator | Memanggil `locationStreamer.start()` di `onCreate` (hanya saat `USE_DEMO_DATA=false`). |
| `app/build.gradle.kts` + `gradle/libs.versions.toml` | Ubah | Dependency + engine | Tambah `supabase-realtime`. **Ganti `ktor-client-android` → `ktor-client-okhttp`** karena engine Android tidak mendukung WebSocket (realtime wajib WebSocket). |

### Cara kerja `PassengerLocationStreamer` (inti Fase 2)

1. Amati session; jika penumpang login dan punya `primary_selection`, ambil `routeId`-nya.
2. Buka channel realtime `trips` (filter `route_id`) untuk tahu kapan ada trip jadi `active`.
3. Query sekali di awal (`SELECT ... WHERE status='active'`) menangkap trip yang sudah aktif
   sebelum subscribe — karena realtime hanya mengirim *perubahan*.
4. Begitu ada trip aktif, buka channel kedua `vehicle_locations` (filter `trip_id`).
5. Tiap event GPS masuk → `writeToRoom` (update lat/lng). Tiap perubahan `trips.status` →
   tulis status ke Room. Room reactive → UI auto-refresh.

### Catatan penting (jebakan yang sudah dilewati)

- **Ktor engine**: `ktor-client-android` pakai `HttpURLConnection` yang **tidak bisa WebSocket**.
  Gejalanya menipu: HTTP (query/auth) jalan, tapi Realtime loop CONNECTING→DISCONNECTED.
  Wajib pakai engine `okhttp` (atau `cio`). Jangan kembalikan ke engine Android.
- **Dua channel, dua jenis update Room**: channel `trips` meng-update field `status` (badge
  kartu), channel `vehicle_locations` meng-update `latitude`/`longitude` (marker peta).

---

## Pemenuhan terhadap `DRIVER_TRIP_AND_LOCATION_PLAN.md`

| Section plan | Status | Keterangan |
| --- | --- | --- |
| 1–5. Secure driver trip (assignment, RPC, lifecycle) | ✅ Selesai | RPC `get_assigned_trip`/`start`/`advance`/`finish`, assigned trip via SQL Editor, RLS. |
| 6. Fused Location Provider | ✅ | Dipakai di `ShuttleSpotLocationService`. |
| 6. Permission saat trip dimulai | ✅ | Di `DriverTripScreens`. |
| 6. Foreground service hanya selama trip aktif | ✅ | Start di `startTrip`, stop di `finishTrip`. |
| 6. Migration RPC `publish_vehicle_location` | ❌ | **Deviasi**: pakai `insert` langsung + RLS, bukan RPC security-definer. |
| 6. Interval 10–15 detik | ⚠️ | Saat ini 5 detik. |
| 6. Pergerakan bermakna (min displacement) | ❌ | Belum ada `setMinUpdateDistanceMeters`. |
| 6. Stop saat logout | ❌ | Hanya stop saat finish trip. |
| 6. State "permission denied / location disabled" | ⚠️ | Denied ada basic; "GPS disabled" belum. |
| 6. Tidak minta background-location | ✅ | Benar, tidak diminta. |
| 7. Location outbox (simpan gagal, retry WorkManager, idempotency) | ❌ | **Belum sama sekali** — pakai drop-on-failure. |
| 7. Cleanup server lokasi >24 jam | ✅ | `delete_expired_vehicle_locations` sudah ada. |
| 8. Subscribe active trip pada route passenger | ✅ | Filter `route_id` + `trip_id`. |
| 8. Terima perubahan trip + lokasi | ✅ | Dua channel realtime. |
| 8. Simpan snapshot ke Room | ✅ | `writeToRoom`. |
| 8. Tampilkan timestamp pembaruan | ⚠️ | `updatedAt`/`isStale` disimpan; UI belum menampilkan eksplisit. |
| 8. Bedakan fresh/stale/offline/completed | ⚠️ | fresh/stale/completed ada; offline belum. |
| 8. Perbarui progress & ETA dari posisi + halte | ❌ | **Belum** — ini status per-passenger (Fase 3). |

Legenda: ✅ selesai · ⚠️ sebagian/deviasi minor · ❌ belum

---

## To-do (belum dikerjakan)

Urut prioritas berdasarkan dampak:

- [ ] **Status & progress per-passenger (Fase 3).** Derive `AT_PICKUP` / `WAITING_AT_OTHERSTOP`
  / `MISSED` / `RIDING` + progress bar + ETA dari posisi bus vs halte pilihan user vs boarding
  intent. Port logika `InMemoryPassengerTripRepository.snapshotFor()` ke `writeToRoom`. Pakai
  `RoutePaths.bsdUmnProgressNear`/`bsdUmnPointAt`. **Paling berdampak ke UX demo.**
- [ ] **Location outbox (Section 7).** Simpan update lokasi gagal ke Room, retry via WorkManager
  saat online, idempotency key, batasi jumlah record, pertahankan titik terbaru. Ketahanan offline.
- [ ] **RPC `publish_vehicle_location` (Section 6).** Ganti direct insert dengan RPC
  security-definer yang memvalidasi lat/lng/accuracy/timestamp dan ambil driver dari `auth.uid()`.
  Hardening keamanan server-side.
- [ ] **Detail GPS (Section 6).** Interval 5s → 10–15s, tambah min displacement, stop saat logout,
  tampilkan state "GPS/lokasi dimatikan".
- [ ] **State UI (Section 8).** Tampilkan "terakhir diperbarui X detik lalu", bedakan state offline
  secara eksplisit di kartu status.
- [ ] **Sticky status.** Saat Fase 3, `MISSED`/`RIDING` harus "nempel" (tidak berubah lagi walau
  bus terus bergerak) — butuh kolom kecil di Room atau tabel derived-state.
- [ ] **Tuning cleanup diagnostic logging.** Setelah realtime stabil, kurangi log verbose `Streamer`
  (sisakan error + event lokasi penting).

### Catatan deviasi sadar

Dua deviasi dari plan adalah keputusan sadar untuk mempercepat demo:
- **Direct insert** (bukan RPC) — fungsional sama, RLS tetap melindungi, tapi validasi
  server-side kurang.
- **Drop-on-failure** (bukan outbox) — lebih simpel, tapi lokasi tidak pulih setelah offline.

Untuk demo "fitur jalan", keduanya tidak memblokir. Untuk kesesuaian plan penuh, keduanya perlu
diperbaiki (lihat to-do #2 dan #3).
