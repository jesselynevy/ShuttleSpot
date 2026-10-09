# Skeleton Guide — Navigasi ShuttleSpot

Panduan alur navigasi (skeleton) aplikasi. Semua kode ada di
`app/src/main/java/com/beg/shuttlespot/navigation/` dan `feature/shell/`.

## 1. Gambaran Besar

Ada **dua level NavHost**:

| NavHost | Dibuat di | Isi |
|---|---|---|
| Root (`navController`) | `ShuttleSpotNavGraph` | Gate, auth, onboarding, shell, flow layar penuh |
| Tab (`tabNav`) | `MainShell` | Empat tab dashboard (Beranda, Rute, Pengumuman, Profil) |

```
RootRoute.GATE ──(cek sesi)──┬─ tidak ada sesi ─▶ AuthRoute.GRAPH
                             └─ ada sesi ───────▶ MainRoute.SHELL

AuthRoute.GRAPH: Welcome ▶ RoleSelection ▶ SignIn/{role}
   ├─ PASSENGER ▶ OnboardingRoute.GRAPH ▶ MainRoute.SHELL
   └─ DRIVER ───────────────────────────▶ MainRoute.SHELL

OnboardingRoute.GRAPH (passenger): Location ▶ Notification* ▶ RouteSelection
   * hanya Android 13+ (API 33); di bawahnya dilewati

MainRoute.SHELL (tab): Beranda | Rute | Pengumuman | Profil
   ├─ PassengerFlowRoute: LiveTrip, BoardingIntent, Notifications
   └─ DriverFlowRoute: ActiveTrip ▶ TripComplete ▶ kembali ke SHELL
```

## 2. Route = `object` Konstanta per Graph

Tidak ada lagi `AppDestination`. Setiap graph punya objek sendiri:

| Object | File | Konstanta |
|---|---|---|
| `RootRoute` | `ShuttleSpotNavGraph.kt` | `GATE` |
| `AuthRoute` | `AuthNavGraph.kt` | `GRAPH`, `WELCOME`, `ROLE_SELECTION`, `SIGN_IN`, `ARG_ROLE`, `signIn(role)` |
| `OnboardingRoute` | `PassengerOnboardingNavGraph.kt` | `GRAPH`, `LOCATION`, `NOTIFICATION`, `ROUTE_SELECTION` |
| `MainRoute` | `MainRoute.kt` | `SHELL`, `HOME`, `ROUTES`, `ANNOUNCEMENTS`, `PROFILE` |
| `PassengerFlowRoute` | `PassengerFlowNavGraph.kt` | `LIVE_TRIP`, `BOARDING_INTENT`, `NOTIFICATIONS` |
| `DriverFlowRoute` | `DriverFlowNavGraph.kt` | `ACTIVE_TRIP`, `TRIP_COMPLETE` |

Route string dipakai dua arah: graph mendaftarkannya (`composable(X)`), graph lain
memakainya untuk `navigate(X)`. Jangan menulis string literal di luar objek ini.

## 3. Komponen Utama

- **`ShuttleSpotNavGraph`** — root `NavHost`, `startDestination = RootRoute.GATE`.
  Menyusun semua graph dan menentukan tujuan setelah login/onboarding selesai
  (`popUpTo(...){ inclusive = true }` agar tombol Back tidak kembali ke layar lama).
- **`SessionGate`** — layar tak terlihat saat start. Membaca `SessionViewModel`:
  Loading → tunggu; Ready + sesi → `SHELL`; Ready tanpa sesi → `AuthRoute.GRAPH`.
- **`authNavGraph(navController, onSignedIn)`** — Welcome, pilih role, sign-in
  (`SignInScreen(isDriver, onSuccess)`). Role dikirim lewat argumen `{role}`.
  Mengembalikan role ke root lewat callback `onSignedIn`.
- **`passengerOnboardingNavGraph(navController, onCompleted)`** — khusus penumpang:
  izin lokasi → izin notifikasi (API 33+) → pilih rute/halte → `onCompleted`.
  Izin notifikasi tidak memblokir bila ditolak ("Lewati untuk sekarang").
- **`mainNavGraph`** — mendaftarkan `SHELL`; membaca sesi dan menampilkan
  `MainShell(role, rootNav)`. Jika sesi hilang (sign-out) → kembali ke auth.
- **`MainShell`** — `Scaffold` + `NavigationBar` dari `tabsFor(role)`, dan
  `NavHost(tabNav)` dengan `HomeTab`, `RoutesTab`, `AnnouncementsTab`, `ProfileTab`.
- **`HomeTab` / `RoutesTab`** — `when (role)` untuk memilih layar per role.
  `rootNav` dipakai untuk membuka flow layar penuh; `tabNav` untuk pindah tab.
- **`passengerFlowNavGraph` / `driverFlowNavGraph`** — layar di luar shell
  (tanpa bottom bar), mis. perjalanan aktif.
- **`RequireRole(role, navController)`** — guard. Bila role sesi ≠ role yang
  diminta, dialihkan ke `AuthRoute.GRAPH` (`popUpTo(0)`). Dipakai di semua layar
  onboarding dan flow khusus role.
- **`SessionViewModel`** — mengamati sesi (`SessionRepository.observeSession()`)
  dan menyediakan `signOut()` (tombol "Keluar" di `ProfileTab`).
- **`NavController.navigateToTab(route)`** — pindah tab dengan
  `saveState` / `restoreState` / `launchSingleTop`.

## 4. Role dan Tab

`UserRole` saat ini: `PASSENGER`, `DRIVER` (admin di luar MVP, lihat `plan.md`).
Tab ditentukan oleh:

```kotlin
fun tabsFor(role: UserRole) = when (role) {
    UserRole.PASSENGER, UserRole.DRIVER -> defaultTabs
}
```

`when` bersifat exhaustive, sehingga menambah role baru memaksa compiler
menunjukkan semua tempat yang harus diisi.

## 5. Cara Menambah Sesuatu

**Role baru (mis. ADMIN)**
1. Tambah ke `UserRole` (`core/model/Session.kt`).
2. Isi cabang di `tabsFor`, `HomeTab`, `RoutesTab`, `ProfileTab`, dan `onSignedIn`
   di `ShuttleSpotNavGraph`; compiler akan menandai yang terlewat.
3. Jika ada flow sendiri, buat `AdminFlowNavGraph.kt` + `object AdminFlowRoute`,
   bungkus layar dengan `RequireRole(UserRole.ADMIN, ...)`.

**Tab baru**
1. Konstanta di `MainRoute`, entri di `enum MainTab` dan `defaultTabs`/`tabsFor`.
2. `composable(MainRoute.XXX)` di `MainShell`.

**Graph/flow baru**
1. File `XxxNavGraph.kt` berisi `object XxxRoute` dan
   `fun NavGraphBuilder.xxxNavGraph(navController, ...)`.
2. Daftarkan di `ShuttleSpotNavGraph`.
3. Tambah `RequireRole` bila khusus role tertentu.

## 6. Konvensi

- Satu graph per file; fungsi berupa extension `NavGraphBuilder`.
- Layar tidak menerima `NavController`; navigasi lewat callback (`onFinished`,
  `onActiveTrip`, dst.) yang dirangkai di nav graph / tab.
- Setelah langkah sekali-jalan (login, onboarding, selesai trip) gunakan
  `popUpTo(...){ inclusive = true }`.
- Layar dengan `hiltViewModel()` dipisah menjadi composable stateful + `*Content`
  stateless agar bisa di-`@Preview` (preview tidak bisa membuat Hilt ViewModel).
- Warna dan teks: ikuti `docs/colors.md`; latar digambar oleh `Surface` di
  `MainActivity`.

## 7. Kesesuaian dengan `plan.md`

| Alur plan.md | Skeleton |
|---|---|
| Welcome → Role → Sign-in | `AuthRoute` |
| Location Assistance → pilih rute/halte → selesai | `OnboardingRoute` → `SHELL` |
| Driver: sign-in → Home → Start Trip → Active Trip → Finish → Trip Complete | `SHELL` → `DriverFlowRoute` |
| Admin | Ditunda (di luar MVP) |

Penyimpangan: ditambah langkah izin notifikasi setelah lokasi; tab "Pengumuman"
terpisah dari layar `Notifications` (ikon lonceng di Beranda penumpang).

## 8. Catatan / Utang Teknis

- Kode skeleton belum diverifikasi compiler di lingkungan pembuat — lakukan Sync + Build.
- `RoutesTab` penumpang: `selectionSaved` di ViewModel bisa membuat status
  "Rute utama siap" muncul lagi saat tab dibuka ulang.
- Penumpang yang kembali tanpa rute tersimpan langsung masuk shell (belum ke onboarding).
- Parameter `onRoutes`, `onAnnouncements`, `onProfile` di `PassengerHomeScreen` belum terpakai.
- `themes.xml` berinduk Material Light → kilatan putih di dark mode (opsi: `values-night/themes.xml`).
- Dependensi `navigation-compose` ganda di `app/build.gradle.kts` (2.8.5 + katalog).
- `docs/three-member-task-board.md` masih menyebut `AppDestination.kt`.
