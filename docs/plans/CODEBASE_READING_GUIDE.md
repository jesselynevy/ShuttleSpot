# Panduan Membaca Codebase ShuttleSpot

Panduan ini menjelaskan hubungan UI, state, navigation, MVVM, dependency injection, Room, Supabase, autentikasi, dan proses background pada implementasi saat ini.

## Urutan membaca yang disarankan

1. Mulai dari `MainActivity.kt`: inisialisasi Compose, theme, MapLibre, dan auth deep link.
2. Lanjutkan ke `navigation/ShuttleSpotNavGraph.kt`: lihat bagaimana session menentukan tujuan awal.
3. Pilih satu flow di folder `feature/`, lalu baca `Screen` dan `ViewModel`-nya bersama-sama.
4. Dari ViewModel, ikuti interface di `data/repository/` menuju implementasinya.
5. Baca `di/RepositoryModule.kt` untuk mengetahui implementasi yang diberikan Hilt.
6. Terakhir, baca `data/local/`, `data/remote/`, dan `data/sync/` untuk memahami Room, Supabase, dan WorkManager.

Alur umum aplikasi:

```text
Composable → ViewModel → Repository interface
                         ├── Room/DAO
                         ├── Supabase data source/RPC
                         └── Demo implementation
```

## Compose layout

`MainActivity` memanggil `ShuttleSpotApp()`. Composable ini memasang `ShuttleSpotTheme`, background, root `NavController`, lalu `ShuttleSpotNavGraph`.

| UI | Composable utama | Komponen penting |
| --- | --- | --- |
| Welcome dan pilihan role | `WelcomeScreen`, `RoleSelectionScreen` | `WaveBackground`, role cards |
| Login dan registrasi | `SignInScreen`, `SignUpScreen`, `CheckEmailScreen` | Form passenger/driver dan password fields |
| Reset/ganti password | `ForgotPasswordScreen`, `NewPasswordScreen`, `DriverPasswordChangeScreen` | Password validation dan visibility button |
| Passenger onboarding | `NotificationPermissionScreen`, `LocationAssistanceScreen`, `RouteSelectionScreen` | Permission launcher, route/stop cards, timetable, review |
| Passenger home | `PassengerHomeScreen` → `PassengerHomeContent` | `AvatarHeader`, `TripSummaryCard`, `BusStatusCard`, `PengumumanHomeCard` |
| Live tracker | `LiveTripDemoScreen` | `OpenStreetMap`, route bar, marker, `BoardingIntentDialog` |
| Driver | `DriverHomeScreen`, `DriverActiveTripScreen` | Assignment, demand, start/advance/finish controls |
| Main tabs | `MainShell` | `Scaffold`, bottom `NavigationBar`, nested tab `NavHost` |

Composable pada folder `core/ui/components/` digunakan ulang oleh beberapa screen. Screen sebaiknya hanya merender state dan mengirim event; akses Room atau Supabase tidak dilakukan langsung dari Composable.

## UI theming

Theme berada di `core/ui/theme/`:

- `Color.kt`: warna dasar, light/dark, dan status.
- `Theme.kt`: membentuk `LightColorScheme`, `DarkColorScheme`, lalu memasangnya melalui `MaterialTheme`.
- `Type.kt`: seluruh typography Material 3 menggunakan font Inter dari `res/font`.
- `Shape.kt`: radius Material 3 dan semantic shape seperti avatar serta status pill.
- `ExtendedColors.kt`: warna tambahan yang tidak tersedia pada `ColorScheme`, diberikan lewat `LocalExtendedColors`.

`ThemeViewModel` membaca `ThemeRepository`. Pilihan dark mode disimpan melalui `SharedPreferences`; jika belum dipilih, aplikasi mengikuti system theme. Dynamic color tersedia di kode tetapi default-nya nonaktif.

## State

Terdapat tiga tingkat state:

1. **Local Compose state** — `remember`/`mutableStateOf` untuk state singkat seperti dialog, password visibility, selected marker, atau map instance. State ini tidak perlu bertahan sebagai data aplikasi.
2. **Screen state** — ViewModel mengekspos `StateFlow<UiState>`, misalnya `AuthUiState`, `RouteSelectionUiState`, `PassengerHomeUiState`, dan `DriverTripUiState`. Ini menjaga data tetap ada saat recomposition dan memisahkan logic dari UI.
3. **Persistent state** — Room menyimpan session cache, route, pilihan utama, trip snapshot, notification, boarding intent, dan outbox agar data penting tetap tersedia setelah aplikasi ditutup.

ViewModel menjalankan coroutine melalui `viewModelScope`. Composable mengamati Flow menggunakan `collectAsState` atau `collectAsStateWithLifecycle`; API lifecycle-aware adalah pola yang sebaiknya digunakan untuk screen baru.

## Navigation

Navigation menggunakan Navigation Compose dengan dua tingkat controller:

- Root controller mengatur auth, onboarding, main shell, passenger flow, dan driver flow.
- Controller di `MainShell` hanya mengatur tab Home, Routes, Notifications, dan Profile.

Alur awal:

```text
MainActivity
→ ShuttleSpotNavGraph
→ SessionGate
   ├── tanpa session → Auth graph
   ├── wajib ganti password → DriverPasswordChange
   └── session valid → MainShell
```

File graph utama adalah `AuthNavGraph.kt`, `PassengerOnboardingNavGraph.kt`, `PassengerFlowNavGraph.kt`, `DriverFlowNavGraph.kt`, dan `MainNavGraph.kt`. `RequireRole` memeriksa role dari session, bukan dari argument UI. Deep link `shuttlespot://auth/callback` dan `shuttlespot://auth/reset-password` diterima `MainActivity`, diteruskan ke Supabase Auth, lalu ke `NavController`.

## MVVM dan dependency injection

Project memakai MVVM dengan pembagian berikut:

- **Model:** tipe domain di `core/model/`, Room entities, dan remote DTO.
- **View:** Composable di folder `feature/` dan `core/ui/components/`.
- **ViewModel:** menggabungkan Flow, memvalidasi event UI, dan memanggil repository.
- **Repository:** kontrak domain yang menyembunyikan apakah data berasal dari Room, Supabase, atau demo.

Pendekatan ini dipakai agar UI tidak bergantung pada SDK database dan agar demo/remote implementation dapat diganti tanpa mengubah screen.

Hilt menghubungkan semua bagian:

- `ShuttleSpotApplication` memakai `@HiltAndroidApp`.
- `MainActivity` memakai `@AndroidEntryPoint`.
- ViewModel memakai `@HiltViewModel` dan constructor injection.
- `DatabaseModule` menyediakan Room dan DAO.
- `SupabaseModule` menyediakan satu `SupabaseClient`.
- `RepositoryModule` menghubungkan interface dengan implementation.

`BuildConfig.USE_DEMO_DATA` memilih demo atau remote pada bagian yang sudah memiliki dua adapter. Driver trip, misalnya, memakai `InMemoryDriverTripRepository` pada demo mode dan `SupabaseDriverTripRepository` pada connected mode.

## Ktor, Kotlinx Serialization, dan Coil

- **Ktor** dipasang sebagai HTTP engine Android untuk Supabase Kotlin. Screen dan ViewModel tidak memanggil Ktor secara langsung.
- **Kotlinx Serialization** digunakan oleh remote DTO (`@Serializable`, `@SerialName`) untuk mengubah response PostgREST/RPC menjadi Kotlin object. `Json` juga membaca payload outbox.
- **Coil tidak digunakan dan tidak terpasang.** Image saat ini berasal dari drawable resources, `painterResource`, atau gambar marker yang dibuat programmatically.

## Native feature

- **Map:** `OpenStreetMap` membungkus native `MapLibre MapView` melalui `AndroidView`, menggunakan tile OpenStreetMap, route line, stop marker, dan bus marker.
- **Permissions:** notification dan foreground location diminta melalui `rememberLauncherForActivityResult` saat flow terkait dibuka.
- **Deep link:** Android intent filter membuka aplikasi untuk confirmation dan reset-password link.
- **Foreground GPS:** permission sudah dideklarasikan, tetapi publisher masih `NoOpLocationPublisher`; service dan fused-location implementation belum tersedia.
- **Push notification:** permission UI tersedia, tetapi Firebase/FCM delivery belum diimplementasikan.

## Database dan aliran data

Android terhubung ke dua lapisan database:

### Room lokal

`ShuttleSpotDatabase` versi 5 memiliki satu `ShuttleSpotDao`. Entities mencakup route, stop, geometry, timetable, primary selection, session, trip snapshot, boarding intent, notification, announcement, dan outbox. Migration Room berada di `DatabaseModule`.

Untuk passenger, Room menjadi data yang diamati UI:

```text
Supabase/demo refresh → PersistedRouteRepository → Room
Room Flow → ViewModel StateFlow → Composable
```

### Supabase remote

URL dan anon key dibaca dari `local.properties` menjadi `BuildConfig`. `SupabaseModule` memasang Auth PKCE dan PostgREST. Remote data source mengambil data, Kotlinx Serialization mengubah DTO, repository memetakan DTO ke domain/Room, lalu UI tetap membaca repository.

Mutation offline berjalan seperti ini:

```text
UI action → Repository menyimpan Room + Outbox
→ WorkManager → OutboxProcessor
→ Supabase RPC → hapus/konfirmasi Outbox
```

Schema remote berada di `supabase/migrations/`, seed di `supabase/seed.sql`, dan akses data dibatasi oleh RLS serta secure RPC. Perubahan schema harus dibuat sebagai migration, bukan langsung di hosted SQL Editor.

Driver trip saat ini berbeda: `SupabaseDriverTripRepository` memanggil secure RPC secara langsung dan mengembalikan `DriverTripSnapshot`; cache Room untuk driver trip belum diterapkan.

## Authentication

Auth menggunakan Supabase Auth dengan PKCE:

- Passenger signup memakai email tepat `@student.umn.ac.id`, password, email confirmation, dan profile pada tabel `profiles`.
- Passenger sign-in memuat profile remote, lalu menyimpan `AppSession` ke Room.
- Driver memasukkan phone/password. Android menormalisasi nomor dan mengubahnya menjadi internal email alias yang tidak ditampilkan ke driver.
- Driver yang masih memakai temporary password diarahkan ke `DriverPasswordChangeScreen` melalui `mustChangePassword`.
- `PersistedSessionRepository` memulihkan session remote, mempertahankan cache saat hanya terjadi gangguan jaringan, dan membersihkan cache ketika remote memastikan session sudah tidak ada.
- Navigation dan `RequireRole` selalu memakai role dari profile/session sebagai sumber kebenaran.

## Background process

Background process yang aktif adalah outbox sync:

- `ShuttleSpotApplication` menjadwalkannya saat app startup pada connected mode.
- Repository menjadwalkannya setelah menyimpan mutation lokal.
- `OutboxSyncScheduler` membuat unique WorkManager job dengan syarat jaringan tersedia.
- `OutboxSyncWorker` memanggil `OutboxProcessor` melalui Hilt entry point.
- Processor mengirim item secara berurutan, menghapus item sukses, dan membatasi retry sampai lima kali.

Belum ada foreground location service, Realtime subscription, FCM service, atau periodic polling.

## Contoh menelusuri satu fitur

Untuk memahami penyimpanan rute passenger, baca berurutan:

```text
RouteSelectionScreen
→ RouteSelectionViewModel
→ RouteRepository
→ PersistedRouteRepository
→ ShuttleSpotDao + OutboxSyncTrigger
→ SupabaseRouteDataSource / SupabaseOutboxRemoteDataSource
```

Untuk driver trip:

```text
DriverHomeScreen / DriverActiveTripScreen
→ DriverTripViewModel
→ DriverTripRepository
→ SupabaseDriverTripRepository
→ secure PostgreSQL RPC
```

Dengan pola tersebut, fitur lain dapat dibaca dari UI ke bawah tanpa perlu memahami seluruh project sekaligus.
