# Bus demo animation (live trip screen)

This documents the bus-moving-along-the-route animation on the live trip screen: the bus
icon travels the actual road from the first stop to UMN, pausing briefly at every stop along
the way, while the progress bar in the bus status card tracks the current leg and the status
label switches between "en route", "waiting at your stop", "waiting at another stop",
"missed" and "riding" depending on where the bus is and whether the passenger has confirmed
boarding. There is no real GPS/backend behind any of this — it's a self-driving demo loop,
built to look right while the real location feed and SSO/driver backend don't exist yet.

## Where it lives

| File | Role |
| --- | --- |
| `data/repository/RoutePaths.kt` | Road geometry for `bsd_umn` as a polyline, plus helpers to go between a progress fraction (0f..1f) and a lat/lng on that road. |
| `data/repository/InMemoryRouteRepository.kt` | **Single source of truth** for the route's halte list (id, name, lat/lng, sequence, scheduled time). Both the halte-picker and the animation read from here. |
| `data/repository/InMemoryPassengerTripRepository.kt` | Owns the demo loop. Pulls halte list via `routeRepository.observeRouteDetails("bsd_umn")` (not hardcoded), walks the bus through all stops in order, pausing at each one, and works out the *per-passenger* `TripStatus` by combining bus position with the passenger's chosen pickup stop and boarding confirmation. |
| `core/model/Trip.kt` | `TripStatus` — the single status enum used everywhere (repository + home-screen + live-trip UI). An older UI-only `BusStatusType` has been removed. |
| `core/ui/components/BusStatusCard.kt` / `BusStatusRow.kt` | Render directly from `TripStatus` — label, color, buttons shown, and the status copy all branch on it. |
| `core/ui/components/BusRouteProgress.kt` | Renders the bar/bus-icon straight from `progress` (no easing — see "Progress bar" below) and takes `fromStopName`/`toStopName` so the dots at each end show the real halte names for the current leg. |
| `feature/passenger/livetrip/LiveTripDemoScreen.kt` | Wires `trip?.status`, `trip?.progress`, `trip?.previousStopName`/`trip?.nextStopName` into `BusStatusCard`. |
| `core/ui/components/OpenStreetMap.kt` | Redraws just the bus marker's `GeoJsonSource` whenever `busLocation` changes. |

Nothing in the ViewModel (`LiveTripViewModel`) or the map drawing code needs to change for
any of this — they already react to `trip.progress` / `trip.status` / `mapState.busLocation`.
Everything described below lives in `InMemoryPassengerTripRepository` (the data) and the
`BusStatusCard`/`BusRouteProgress` components (how the data is drawn).

## 1. Point-on-the-road interpolation (`RoutePaths.kt`)

The route's road geometry (`BSD_UMN`) is a polyline of lat/lng points (simplified from an
OSRM driving-route response). Two helpers built on it:

- **`bsdUmnPointAt(progress: Float): GeoPoint`** — turns a route-wide progress fraction
  (0f = first stop, 1f = UMN) into an actual coordinate *on the road*, by walking the
  polyline's precomputed cumulative distance (`cumulativeMeters`, cached via `by lazy`) and
  linearly interpolating within whichever segment the target distance falls into.
- **`bsdUmnProgressNear(target: GeoPoint): Float`** — the reverse: given any lat/lng, finds the
  closest point on the polyline and returns *that* point's cumulative-distance fraction.
  This is how the repository turns "halte N's coordinate" into "halte N's progress along the
  route" without anyone having to hand-measure or hardcode it.

Both are pure math (Haversine + linear interpolation), no network calls.

## 2. The demo loop (`InMemoryPassengerTripRepository.kt`)

### 2a. Halte list comes from `RouteRepository`, not a constant

The repository takes a `RouteRepository` via `@Inject constructor` and derives its halte list
from it instead of carrying its own hardcoded copy:

```kotlin
private data class ResolvedStop(
    val id: String,          // from Stop.id, so pickupStopId can match directly
    val name: String,        // for previousStopName/nextStopName in the snapshot
    val point: GeoPoint,     // for RoutePaths.bsdUmnPointAt() lookup
    val routeProgress: Float // pre-computed via RoutePaths.bsdUmnProgressNear(point)
)

private val stopsFlow: Flow<List<ResolvedStop>> =
    routeRepository.observeRouteDetails(ROUTE_ID)
        .map { details ->
            details?.stops.orEmpty()
                .sortedBy { it.sequence }
                .mapNotNull { stop ->
                    val lat = stop.latitude ?: return@mapNotNull null
                    val lng = stop.longitude ?: return@mapNotNull null
                    val point = GeoPoint(lat, lng)
                    ResolvedStop(stop.id, stop.name, point, RoutePaths.bsdUmnProgressNear(point))
                }
        }
        .distinctUntilChanged()
```

Previously this repository carried its own `DEMO_STOPS` constant that duplicated the halte
list in `InMemoryRouteRepository`'s `bsd_umn` route — same coordinates, same names, same ids.
That duplication is now gone: edit the stops in `InMemoryRouteRepository.routeDetails[0]`
and the animation picks up the change on the next collection (in practice: next app launch,
since the route list is static).

A few things to note:
- Halte without coordinates (nullable `latitude`/`longitude` from `Stop`) are **skipped**,
  not crashed on. If this makes `stops.size < 2`, the animation simply doesn't start.
- The list is `sortedBy { it.sequence }` to guarantee the order matches the polyline's
  direction, independent of how the `Stop` entries happen to be declared.
- `distinctUntilChanged()` means `stopsFlow` emits once (on subscription) and then stays
  quiet unless the halte list actually changes — which, with the current in-memory
  repository, it never does.

### 2b. Bus position (`busTickFlow(stops)`)

```kotlin
private fun busTickFlow(stops: List<ResolvedStop>): Flow<BusTick> = flow {
    if (stops.size < 2) return@flow
    var routeProgress = 0f
    while (true) {
        delay(DEMO_TICK_MILLIS)
        val next = routeProgress + (DEMO_TICK_MILLIS.toFloat() / DEMO_FULL_ROUTE_DURATION_MILLIS)
        val arrivedIndex = stops.indexOfFirst { routeProgress < it.routeProgress && next >= it.routeProgress }
            .takeIf { it >= 0 }
        if (arrivedIndex != null) {
            routeProgress = stops[arrivedIndex].routeProgress
            emit(BusTick(routeProgress, isAtStop = true, arrivedStopIndex = arrivedIndex))
            delay(DEMO_STOP_DWELL_MILLIS)
            if (routeProgress >= 1f) break
            continue
        }
        routeProgress = next
        if (routeProgress >= 1f) break
        emit(BusTick(routeProgress, isAtStop = false, arrivedStopIndex = null))
    }
}
```

A `flow { }` (not a `val`, because it needs `stops` passed in) that:

1. **Advances `routeProgress`** every `DEMO_TICK_MILLIS` at a rate set by
   `DEMO_FULL_ROUTE_DURATION_MILLIS` (how long the whole one-way trip, first stop → UMN,
   takes).
2. **Detects crossing a stop.** Each tick checks whether the step from the current position
   to the next would cross one of the `stops`' pre-computed `routeProgress` values. If so,
   it snaps exactly onto that stop (no overshoot), emits a tick with `isAtStop = true`, and
   `delay(DEMO_STOP_DWELL_MILLIS)` — the bus visually "stands still" at that stop.
3. **Stops for good at UMN.** When the stop just arrived at is the last one, the loop
   `break`s — the bus makes one trip, start to finish, and stays parked at UMN.

`busTickFlow` only knows about the bus's own position — it has no idea which stop belongs
to which passenger, or whether anyone has confirmed boarding. That's handled one layer up.

### 2c. Per-passenger status (`snapshotFor`)

```kotlin
private val activeTrip: StateFlow<ActiveTripSnapshot?> =
    stopsFlow.flatMapLatest { stops ->
        if (stops.size < 2) flowOf<ActiveTripSnapshot?>(null)
        else combine(busTickFlow(stops), primarySelectionFlow, intent) { tick, selection, boardingIntent ->
            snapshotFor(tick, stops, selection, boardingIntent)
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, null)
```

Four flows meet here:

- **`stopsFlow`** (outer, `flatMapLatest`): if halte list ever changes, cancel the inner
  pipeline and restart it with the new list — no tick ever operates on stale stops.
- **`busTickFlow(stops)`**: new bus position.
- **`primarySelectionFlow`** (`routeRepository.observePrimarySelection()`): user's chosen
  pickup halte.
- **`intent`**: user's boarding intent (same `MutableStateFlow` used by
  `submitBoardingIntent`/`observeBoardingIntent`).

Any change in those three inner flows retriggers `snapshotFor()`, which turns
`(tick, stops, selection, boardingIntent)` into a `TripStatus` for *this* passenger:

| Situation | Status |
| --- | --- |
| No pickup stop saved yet | `AT_PICKUP` while bus is stopped (anywhere), `EN_ROUTE` while moving — generic fallback. |
| Bus stopped exactly at the passenger's pickup stop | `AT_PICKUP`, or straight to `RIDING` if boarding was already konfirmed. Confirming *during* the dwell also flips it to `RIDING` immediately, since `intent` changing re-fires `combine`. |
| Bus stopped at any other halte | `WAITING_AT_OTHERSTOP`. |
| Bus has actually left the pickup stop (`legIndex` moved past it), no confirmation | `MISSED`. |
| Bus has left the pickup stop and boarding was confirmed | `RIDING`. |

`MISSED` and `RIDING` are **sticky**: once set, `snapshotFor()` keeps returning that same
status on every later tick, ignoring further bus movement — a passenger who missed the bus
stays "missed" for the rest of this trip, and someone riding stays "riding" even as the bus
continues toward later stops. The sticky value is tracked in two `@Volatile var`s
(`stickyStatus`, `stickyForPickupStopId`) and is reset automatically if the passenger's
saved `pickupStopId` changes to a different stop (so switching your chosen halte doesn't
leave you stuck with a stale "missed"/"riding" from before).

### 2d. Initial value trade-off

Previously, `stateIn`'s `initialValue` was a synchronous `snapshotAt(0f)` — possible
because `DEMO_STOPS` was a hardcoded constant available at construction time. Now that
`stops` comes from a flow, there's no synchronous value to build one from, so
`initialValue = null`.

In practice this doesn't matter: `InMemoryRouteRepository` returns a `MutableStateFlow` that
emits its current value synchronously on subscription, so `stopsFlow` emits within
microseconds of collection starting, and the first non-null snapshot follows one
`DEMO_TICK_MILLIS` (~25ms) later. Collectors that treat `trip?` as nullable (which every UI
site already does) see this correctly.

The one place this does show up is the repository's own unit tests: the first test now reads
`.observeActiveTrip("bsd_umn").filterNotNull().first().routeId` instead of
`.first()?.routeId`, to wait past the initial null emission.

### Swapping in a real location/backend feed later

Replace `busTickFlow` with whatever collects from the real bus-position source.
`stopsFlow`/`primarySelectionFlow`/`intent` are already backed by real repository calls
(`observeRouteDetails`, `observePrimarySelection`, `observeBoardingIntent`), so only
`busTickFlow` is demo-only. Nothing downstream (ViewModel, `RouteMapUiState`,
`OpenStreetMap`, `BusStatusCard`) needs to change — they only depend on
`PassengerTripRepository.observeActiveTrip(routeId): Flow<ActiveTripSnapshot?>`.

## 3. One status enum, not two (`TripStatus`)

```kotlin
// core/model/Trip.kt
enum class TripStatus {
    NO_ACTIVE_BUS, EN_ROUTE, AT_PICKUP, WAITING_AT_OTHERSTOP, MISSED, RIDING, COMPLETED, NOT_STARTED
}
```

There used to be a second enum, `BusStatusType`, defined purely for the UI, with a
`TripStatus?.toBusStatusType()` mapping function in `LiveTripDemoScreen.kt` that converted
between the two. It duplicated `TripStatus` almost one-to-one and had to be kept in sync by
hand. Both the enum and the mapping function are gone now. `BusStatusCard`, `BusStatusRow`
and `ButtonsToShow` all take `busStatus: TripStatus` directly and branch on it exhaustively
(including `COMPLETED`/`NOT_STARTED`, which `BusStatusType` never had equivalents for).
`LiveTripDemoScreen.kt` just passes `trip?.status ?: TripStatus.NO_ACTIVE_BUS` straight into
`BusStatusCard`.

`PassengerHomeViewModel`'s `PassengerTripStatus` (a *third*, home-screen-only enum that also
mirrors `TripStatus` 1:1) was left in place but given a matching `WAITING_AT_OTHERSTOP` case
so its `when` over `TripStatus` stays exhaustive. Collapsing this one too is a possible
future cleanup.

## 4. Progress bar & halte labels (`BusStatusCard.kt`, `BusRouteProgress.kt`)

`BusRouteProgress`'s bar, fill, and bus icon all read `progress.coerceIn(0f, 1f)`
**directly** — no `animateFloatAsState`/`tween` wrapping it. An earlier version did wrap it
in a 800ms tween to smooth per-tick jumps, which caused two visible problems once the bus's
target position kept changing every tick and occasionally reset (new leg): the animated
value permanently lagged the real target during a climb (so the bar never quite reached 0 or
1), and the moment `progress` reset from ~1 back to ~0 (new leg), the eased value visibly
"caught up" fast or seemed to slide backwards. Since the repository already emits
fine-grained updates every `DEMO_TICK_MILLIS`, the raw value alone reads as smooth motion.

`BusRouteProgress` also takes `fromStopName`/`toStopName` (with sensible defaults so other
call sites that don't pass them stay unchanged). `BusStatusCard` forwards
`trip?.previousStopName`/`trip?.nextStopName` through — `previousStopName` is a field on
`ActiveTripSnapshot` filled by the repository as `stops[legIndex].name`. The two dots on the
progress bar now show the actual names of the current leg's start/end halte (e.g. "Halte
Assobirin" / "Halte Bundaran Alam Sutera"), updating automatically as the bus moves from
leg to leg.

## 5. Bus marker on the map (`OpenStreetMap.kt`)

Unchanged by any of the above. It already:

- Derives the bus marker from `markers.firstOrNull { it.kind == MarkerKind.Bus }`, sourced
  from `mapState.busLocation`.
- Has `LaunchedEffect(map, bus) { source.setGeoJson(...) }`, scoped to *only* the bus
  marker's `GeoJsonSource` — separate from the effect that rebuilds the route line/stop
  circles, which doesn't re-run on every tick because `stopMarkers` stays structurally equal
  between ticks (only the bus entry changes).

Movement is a plain jump from point A to point B each tick, no easing — at the current
`DEMO_TICK_MILLIS` this reads as smooth enough for a demo. A Compose `Animatable` wrapping
the bus's lat/lng inside `OpenStreetMap` would be the next step if it ever needs to look
smoother.

## 6. Tuning knobs

All in `InMemoryPassengerTripRepository`'s `private companion object`:

| Constant | Meaning |
| --- | --- |
| `ROUTE_ID` | Which route the demo animates (currently `"bsd_umn"` — the only one with road geometry wired up). |
| `TRIP_ID` | Static trip id for the demo snapshot. Matched against `BoardingIntent.tripId` to decide if boarding is confirmed. |
| `DEMO_TICK_MILLIS` | How often a new position is emitted. Smaller = smoother motion, more frequent recompute. |
| `DEMO_FULL_ROUTE_DURATION_MILLIS` | How long the whole one-way trip (first stop → UMN) takes. |
| `DEMO_STOP_DWELL_MILLIS` | How long the bus "waits" at each halte before continuing. |
| `DEMO_LEG_ETA_MINUTES` | Cosmetic ETA ceiling shown at the start of a leg. |

## Known limitations (fine for a demo, flagged for later)

- **One-way only.** The bus makes a single first-stop → UMN trip and then stays parked.
  There's no return trip or re-loop; restarting the app (or whatever eventually re-creates
  the repository) starts it over.
- **ETA is cosmetic** — a linear estimate scoped to the current leg, not a real
  time-to-arrival calculation.
- **The loop runs for the life of the process.** `repositoryScope` is a self-owned
  `CoroutineScope` that's never cancelled (`SharingStarted.Eagerly`), so the coroutine
  keeps running (and eventually `break`s once at UMN) regardless of whether any screen is
  observing it. Negligible cost for a demo singleton; switch to
  `SharingStarted.WhileSubscribed(...)` if it ever needs to pause when nothing is watching.
- **A known geometry glitch near Halte Seberang Telkom.** The underlying OSRM road data for
  this leg detours into a small dead-end/cul-de-sac twice (once legitimately, to reach the
  stop; once redundantly, before continuing on), which shows up as a visible
  loop-within-a-loop on the map in that area. Not yet cleaned up in `RoutePaths.BSD_UMN` —
  see "To-do" below.
- **Unit tests**: `InMemoryPassengerTripRepositoryTest` and `LiveTripViewModelTest` now
  construct the repository with an `InMemoryRouteRepository()` instance (previously they
  passed `null`, which no longer compiles — Hilt doesn't allow default values on `@Inject`
  constructors). Only one assertion needed a change: the "route filter" test uses
  `.filterNotNull().first()` to wait past the null initial value. Not run in this
  environment (no Android SDK here) — please run `./gradlew testDebugUnitTest` to confirm.

## To-do (not implemented yet)

- [ ] **Clean up the Halte Seberang Telkom geometry.** Remove the redundant second dip into
  the dead-end street in `RoutePaths.BSD_UMN`'s encoded polyline so the map doesn't show a
  loop-within-a-loop there. (Already diagnosed — the fix is to splice out the duplicate
  out-and-back segment and re-simplify/re-encode the polyline — just not applied yet.)
- [ ] **Round trip / re-loop option.** Currently the bus stops for good at UMN. If a
  looping demo is wanted again later, add a way to either restart the walk from the first
  stop, or reverse and drive back.
- [ ] **Smoother map marker.** Wrap the bus's lat/lng in a Compose `Animatable` inside
  `OpenStreetMap` and `animateTo` each tick instead of snapping, if the current
  jump-per-tick motion ever looks too stepped.
- [ ] **Collapse `PassengerTripStatus` into `TripStatus`.** The home screen still goes
  through its own near-identical enum rather than `TripStatus` directly, mirrored by a
  `when` mapping. Removing it would complete the "one status enum" cleanup already done for
  `BusStatusType`.
