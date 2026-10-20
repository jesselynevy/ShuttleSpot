# ShuttleSpot Feature Development Tutorial

This guide explains how to add a feature to the current ShuttleSpot Android prototype without breaking the shared passenger flow. It is written for a teammate who is new to the project.

## Read this first

Before editing code, read these files in order:

1. `AGENTS.md`
2. `docs/source-of-truth.md`
3. `docs/ui-first-implementation-plan.md`
4. The relevant sections of `docs/product.md`, `docs/design.md`, and `docs/architecture.md`

The present delivery is a **UI-first prototype**. Its source of data is deterministic in-memory repositories. Do not add a Supabase client, use real GPS publishing, schedule WorkManager, add FCM, or add credentials while building these features.

## The architecture to follow

Every feature follows this direction:

```text
Compose Screen → ViewModel → Repository interface → InMemory repository → Demo models/state
```

The screen only renders `UiState` and sends user actions to the ViewModel. It must not call a repository, Supabase, Room, location service, or map API directly.

```text
Good
Button(onClick = viewModel::confirm)

Avoid
Button(onClick = { repository.submitBoardingIntent(...) })
```

### Important locations

| What you need | Location |
| --- | --- |
| Pure domain models | `app/src/main/java/com/beg/shuttlespot/core/model/` |
| Reusable Compose components and theme | `app/src/main/java/com/beg/shuttlespot/core/ui/` |
| Repository interfaces | `app/src/main/java/com/beg/shuttlespot/data/repository/` |
| Demo data implementations | `app/src/main/java/com/beg/shuttlespot/data/repository/InMemory…Repository.kt` |
| Dependency bindings | `app/src/main/java/com/beg/shuttlespot/di/RepositoryModule.kt` |
| Feature screens/ViewModels | `app/src/main/java/com/beg/shuttlespot/feature/` |
| Navigation routes and wiring | `app/src/main/java/com/beg/shuttlespot/navigation/` |
| Unit tests | `app/src/test/java/com/beg/shuttlespot/` |
| UI-first scope and acceptance criteria | `docs/ui-first-implementation-plan.md` |

## The standard feature recipe

Use this sequence for every new passenger feature.

### 1. Define or reuse the model

First check `core/model` before creating a new model. Reuse an existing type when it describes the data correctly: `Route`, `Stop`, `RouteSelection`, `ActiveTripSnapshot`, `BoardingIntent`, and `TripStatus` are already available.

Make a new model only if it represents a real feature concept not already covered. Keep it plain Kotlin: no Compose and no Android dependencies.

```kotlin
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val routeId: String? = null,
    val publishedAtEpochMillis: Long
)
```

### 2. Add a narrow repository contract

The ViewModel depends on an interface, never on an in-memory implementation. Keep functions small and specific.

```kotlin
interface AnnouncementRepository {
    fun observeAnnouncements(routeId: String?): Flow<List<Announcement>>
}
```

Use `Flow` for information that can change while a screen is open. Use `suspend` functions for user actions that change state, such as saving a route or confirming boarding.

If the feature extends routes, passenger trips, sessions, or driver trips, add the method to the existing repository interface instead of making an overlapping repository.

### 3. Implement deterministic demo data

Implement the interface in an `InMemory…Repository`. Use fixed values, fixed IDs, and fixed timestamps so screenshots and tests stay repeatable.

```kotlin
@Singleton
class InMemoryAnnouncementRepository @Inject constructor() : AnnouncementRepository {
    private val announcements = MutableStateFlow(
        listOf(
            Announcement(
                id = "announcement-delay-bsd",
                title = "Perubahan jadwal rute BSD",
                body = "Keberangkatan berikutnya mengalami keterlambatan sekitar 10 menit.",
                routeId = "bsd_umn",
                publishedAtEpochMillis = 1_791_360_000_000L
            )
        )
    )

    override fun observeAnnouncements(routeId: String?): Flow<List<Announcement>> =
        announcements.map { list -> list.filter { it.routeId == null || it.routeId == routeId } }
}
```

Do not introduce `System.currentTimeMillis()` for demo state unless the UI specifically needs a current clock. Fixed timestamps make tests and mockup captures stable.

### 4. Bind a new repository in Hilt

Only do this when creating a new repository interface. Add the binding in `RepositoryModule.kt`.

```kotlin
@Binds
@Singleton
abstract fun bindAnnouncementRepository(
    implementation: InMemoryAnnouncementRepository
): AnnouncementRepository
```

If you forget this, the app will fail at startup because Hilt cannot construct the ViewModel.

### 5. Create a screen-specific ViewModel

A ViewModel owns screen state and feature actions. Expose an immutable `StateFlow` of a screen-specific UI model, not raw repository/domain data.

```kotlin
data class AnnouncementsUiState(
    val isLoading: Boolean = true,
    val announcements: List<Announcement> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    repository: AnnouncementRepository
) : ViewModel() {
    val state: StateFlow<AnnouncementsUiState> = repository
        .observeAnnouncements(routeId = null)
        .map { AnnouncementsUiState(isLoading = false, announcements = it) }
        .catch { emit(AnnouncementsUiState(isLoading = false, errorMessage = "Pengumuman belum dapat dimuat.")) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AnnouncementsUiState()
        )
}
```

Use Indonesian text for user-visible copy. Keep errors useful and operational. Do not expose exception messages to the passenger.

### 6. Build a passive Compose screen

The screen obtains its ViewModel through Hilt, collects state, renders all relevant states, and passes callbacks to UI elements.

```kotlin
@Composable
fun AnnouncementsScreen(
    viewModel: AnnouncementsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingFeedback()
        state.errorMessage != null -> ErrorFeedback(message = state.errorMessage)
        state.announcements.isEmpty() -> EmptyFeedback("Belum ada pengumuman.")
        else -> AnnouncementList(items = state.announcements, onBack = onBack)
    }
}
```

Follow the visual rules in `docs/design.md`:

- Use the Material 3 theme and the existing reusable controls.
- Keep a 4dp spacing rhythm and aim for 48dp touch targets.
- Do not express a bus status using only color; add an icon and text.
- Keep maps supportive. ETA, status, stops, and last-updated information must be understandable without the map.
- Keep announcements distinct from personal notifications.

### 7. Wire the screen into navigation

Add a destination to `AppDestination.kt` only when one does not already exist. Then replace the matching placeholder in `ShuttleSpotNavGraph.kt` after the screen is ready.

Passenger-only destinations must remain inside:

```kotlin
RequireRole(UserRole.PASSENGER, navController) {
    YourPassengerScreen(...)
}
```

Do not navigate to driver destinations from a passenger screen, and do not put role checks inside individual buttons.

### 8. Test the behavior

Add focused unit tests next to the feature. Test the ViewModel mapping and repository behavior before adding UI tests.

Examples:

- Route selection: missing pickup/destination, same stop, backward stop order, valid save.
- Passenger Home: no-bus, en route, at pickup, missed, riding, completed, stale/error state.
- Boarding: retry does not duplicate an intent; cancel updates the existing intent.
- Announcements: empty state, route filtering, repository error.

Run these commands from the repository root:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Do not run multiple Gradle builds concurrently in the shared worktree. Wait for another build to finish first.

## Existing patterns to copy

### Route-selection pattern

Use `feature/passenger/route/RouteSelectionViewModel.kt` and `RouteSelectionScreen.kt` as the pattern for a multi-step form:

1. Observe data from `RouteRepository`.
2. Store only transient choices in a `UiState`.
3. Validate before save.
4. Save via `RouteRepository` in `viewModelScope`.
5. Have the screen call its navigation callback only after the ViewModel reports success.

### Passenger Home pattern

Use `feature/passenger/home/PassengerHomeViewModel.kt` as the pattern for translating domain status into display state. The screen should not decide what `TripStatus.MISSED` means; the ViewModel maps it to an Indonesian label, allowed actions, ETA/progress visibility, and other screen-ready fields.

### Live Tracker pattern

Use `feature/passenger/livetrip/LiveTripViewModel.kt` and `LiveTripDemoScreen.kt` as the pattern for a map-backed demo feature:

- Read a simulated `ActiveTripSnapshot` through `PassengerTripRepository`.
- Render a fixed/demo location with the existing map component.
- Always show an honest last-updated value.
- Never say that the demo uses live GPS, Realtime, or a driver location publisher.

### Boarding-intent pattern

Use `feature/passenger/boarding/BoardingIntentViewModel.kt` and `BoardingIntentScreen.kt` as the pattern for a confirm/cancel action:

1. Observe the active trip and existing intent.
2. Open a confirmation dialog from the UI.
3. Call the repository once from the ViewModel.
4. Disable duplicate confirmation while submitting or already confirmed.
5. Show confirmed and cancelled states clearly.

## Route and trip state: avoid a common integration bug

The passenger flow must use the saved primary route and pickup stop. Do not introduce a new hard-coded route such as `"bsd_umn"` or stop such as `"bsd_stop"` in a new screen.

The correct dependency is:

```text
Route selection saved in RouteRepository
        ↓
ViewModel observes RouteSelection
        ↓
ViewModel asks PassengerTripRepository for that route's trip
        ↓
Home / Live Tracker / Boarding render the same selection
```

When changing this behavior, update the repository contract and its in-memory test data together. Do not solve it by changing only screen text.

## Location and maps in the prototype

Use **MapLibre Android** for the map. Do not add Leaflet GeoSearch: it is a web/JavaScript solution and would require an unnecessary WebView in this Compose Android app.

For the P0 prototype:

- Request passenger location only after the passenger chooses `Izinkan lokasi`.
- Use location only to rank the fixed demo stops in the ViewModel.
- Keep manual stop selection available after denial or skip.
- Use stable demo coordinates for route/stop/bus markers.
- Do not request background location or start a foreground service.

## Before requesting review

Use this checklist before saying a feature is complete:

- [ ] It matches the P0/P1 scope in `docs/ui-first-implementation-plan.md`.
- [ ] The screen does not call repositories or Android services directly.
- [ ] The ViewModel exposes an immutable UI state and owns actions/validation.
- [ ] Data comes from an interface and deterministic in-memory repository.
- [ ] Loading, empty, error, and any relevant permission-denied state are visible.
- [ ] Passenger pages remain protected by `RequireRole`.
- [ ] No backend, credential, or production-GPS claim was added.
- [ ] Focused tests and `:app:testDebugUnitTest` pass.
- [ ] The flow was checked manually on an emulator at a narrow phone width.
- [ ] The related documentation/roadmap is updated if feature scope changed.

## What not to build yet

These are deliberately deferred until after the UI-first phase:

- Real Supabase authentication, Postgres queries, Realtime, or migrations.
- Room cache, outbox, WorkManager sync, and offline mutation handling.
- Firebase Cloud Messaging.
- Continuous driver GPS, a foreground location service, and live location publishing.
- Admin/staff screens, free-form driver chat, payments, and ticketing.

If a proposed feature needs one of these capabilities, make the UI work with in-memory data first and document the real integration as planned work.
