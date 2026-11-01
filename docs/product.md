# Product: ShuttleSpot

## Purpose

UMN shuttle passengers currently may wait without reliable real-time information about where a bus is, when it will arrive, or whether service has changed. Information is often shared manually, drivers cannot clearly see waiting demand, and operations staff repeatedly answer the same schedule/status questions.

ShuttleSpot is a role-based Android app that makes shuttle operations visible: drivers share trip location/status, passengers follow a selected route and get timely alerts, and staff publish service information.

## Evidence from the prototype research

- Interviews with two active student riders and one intern found recurring uncertainty about actual bus schedules, bus position, and arrival time. The source itself notes that this small interview group is not fully representative.
- Riders may arrive early and wait for a long time, or miss a bus that has already passed their stop.
- The prototype research identified manual driver-dependent updates and repeated operational questions to admins as core operational pain points.
- User testing with six respondents produced an overall usability average of 3.78/4. The journey progress bar scored 4.00/4, so it remains a must-have visual alongside the live map.
- The original prototype was designed using User-Centered Design, interviews, observation, and usability testing. Treat its claims as project research; do not invent statistics not present in the source report.

Source: [`docs/source-of-truth.md`](source-of-truth.md), curated from the team's IF450 ShuttleSpot report.

## Users and roles

| Role | Primary need | MVP permissions |
| --- | --- | --- |
| Passenger (student) | Know whether and when to go to the stop | Read routes, buses, ETAs, announcements; set route/stop; signal boarding intent |
| Driver | Run an assigned trip and provide reliable status | Start/end trip, publish foreground location, progress stops, publish trip announcement |
| Admin/staff | Communicate and observe operations | Read fleet/trip state, create announcements, maintain route data |

## Core flow: passenger MVP

The detailed authority is the approved [Flow ShuttleSpot](https://docs.google.com/document/d/17YCPWTSPf85NLzikkpIhXh7TjIyRhv21k0eYmK0YhJ4/edit?usp=sharing) journey, summarized in `docs/source-of-truth.md`.

1. A passenger chooses `Mahasiswa`, enters a demo SSO username and password input, and continues only with the configured in-memory demo account. This remains a prototype interaction until real authentication is implemented.
2. They proceed through a notification-permission explanation that distinguishes bus-status alerts from general announcements, then optional location permission, with a manual route/stop fallback. Choosing `Aktifkan notifikasi` opens Android's native permission prompt; skipping remains available.
3. They select a route, pickup stop, destination, and desired time; inspect the route map; review the chosen details; and see registration success.
   The current onboarding route picker has two steps: choose a directional demo route, then choose the pickup and a later destination from its ordered stops. Selecting `Pilih Rute` opens the Review screen, which summarizes the route and shows the selected pickup and destination times derived from the available demo timetable.
   Confirming the Review screen saves the route and opens the registration-completion screen with confetti; `Oke!` opens Home. Later route changes retain their separate saved-route confirmation.
4. Home displays the signed-in passenger's display name, primary route, and pickup stop from the saved onboarding selection. Before a route is saved, those summary fields show `Belum pilih`. Home also displays boarding status, announcements, and either no-bus or active-bus status.
5. The passenger can open a tracker and confirm boarding once. A confirmed intent disables further confirmation on Home and Live Trip and changes Home's `Hari ini naik?` status to `Naik`; Home and Tracker then show the confirmed state and handle arrived/missed-bus states.
6. The Route tab displays the saved primary route, pickup, destination, and schedule. Without a saved route it offers `Pilih Rute`; selecting it reuses route/stop selection without onboarding progress. Reviewing a replacement uses `Tetapkan sebagai rute utama`, after which Home updates from the same saved selection.

The first welcome screen introduces ShuttleSpot with the UMN shuttle visual and sends users to role selection through `Masuk`. It explains that university accounts are for students, while driver access is provisioned by an administrator.
In the current passenger-focused build, selecting `Supir Bus` highlights the role and shows that it is not yet available; the continue button remains disabled.

## Core flow: driver MVP

1. The driver enters their registered phone number. The app grants demo driver access only when its normalized value matches a pre-registered driver profile, then opens the assigned trip. This lookup is not phone-number ownership verification.
2. They start the trip; while actively driving, the app publishes location and trip status with a visible foreground-service notification.
3. At each stop they confirm progress and can see the count of passenger boarding intents.
4. They end the trip and location sharing stops.

## Must-have (final-exam MVP)

- Passenger onboarding as specified by the approved Flow ShuttleSpot document: role selection, student email/password inputs, notification/location permission steps, route setup, review, and success.
- Route, origin, destination, time, and schedule browsing; a passenger can save and later replace one primary route selection.
- Home and Tracker states for no active bus, active/en-route bus, arrived/waiting, confirmed boarding, and missed bus.
- Map route/stops/vehicle presentation with ETA, progress, and honest `last updated` information.
- One idempotent passenger boarding intent for the selected active trip and pickup stop.
- A distinct general-announcement preview and personal notification surface.
- Passenger P0 screens sufficient for the capture-ready MVP flow; driver UI is P1 after passenger P0.

Current implementation uses deterministic demo route/trip data which seeds the Room cache. Non-secret sessions, cached route data, saved primary route selection, latest trip state, boarding intents, and the personal notification inbox persist in Room. Route selection and boarding intents queue outbox mutations, while the worker remains disabled in demo mode. Supabase, FCM delivery, real driver GPS publishing, remote sync, and real authentication remain planned integrations, not completed MVP claims.

## Native-device requirements

**Primary native feature: GPS/location.** The driver app uses Android fused location updates during an active trip. A passenger may optionally use device location to identify nearby stops. Location is never collected outside an active driver trip or explicit passenger action.

**Secondary native feature: push notifications.** Use Firebase Cloud Messaging for route announcements and bus-arrival alerts. Notifications must deep-link into the relevant app screen.

## Out of scope for MVP

- Payments, ticketing, turn-by-turn navigation, and public-transport integrations.
- Automatic passenger detection, facial recognition, NFC check-in, or exact occupancy sensing.
- Free-form driver chat/chatbot, multilingual content management, and advanced schedule optimisation. If messaging is later added, use predefined passenger responses and block driver interaction while a trip is active.
- Replacing UMN's actual operational systems; demo data is acceptable unless approved access is available.

## Acceptance criteria

- A passenger can select a route and stop, reopen the app offline, and still see cached route/stop information and the last known bus update labelled with its timestamp.
- An active driver trip publishes a location update that passengers subscribed to that route can receive through realtime data.
- Ending a trip stops active location publishing and marks the trip complete.
- A passenger's boarding intent is stored locally if offline and is synced once connectivity returns; duplicate retries do not create duplicate intents.
- An announcement visible to a route appears in the passenger feed and can trigger a push notification.
- The app includes at least 10 distinct main mockup/implemented screens covering the passenger core flow and a minimal driver flow.
