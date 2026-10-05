# ShuttleSpot planned Supabase data storage

## Current MVP authority

The approved [Flow ShuttleSpot](https://docs.google.com/document/d/17YCPWTSPf85NLzikkpIhXh7TjIyRhv21k0eYmK0YhJ4/edit?usp=sharing), summarized in `docs/source-of-truth.md`, is the authoritative passenger MVP flow. The current milestone delivers that journey as a deterministic in-memory UI prototype.

The Supabase/Room/Realtime/FCM content below is the **planned online-storage roadmap and proposal explanation**. It is not evidence that those backend capabilities are implemented in the current app. Use `Planned Supabase` and a dashed planned-integration connector in flowcharts until a remote adapter has been implemented and tested.

## Proposal requirement

The proposal must explicitly state an online data-storage plan; this is a pass/fail checklist item. It should also show database flow in the overall flowchart and at least one data-related mockup explanation. Offline-first Room sync is an extra-credit capability. :codex-file-citation{path="C:\Users\User\Downloads\IF570_Project_Proposal_Assignment_Rubric (3).pdf" purpose="source"}

## Supabase role in the app

- **Supabase Auth:** manages demo passenger and registered-driver sessions.
- **Supabase Postgres:** the online source of truth for profiles, routes, stops, trips, GPS updates, boarding intent, announcements, and notification subscriptions.
- **Supabase Realtime:** sends active-trip, bus-location, and boarding-intent updates to subscribed passengers and assigned drivers.
- **Supabase RLS:** passengers access their own profile/intents and public active-route data; drivers update only their assigned trip and location.

## Flow additions

1. **Sign-in**
    - Passenger email-domain gate or registered-driver phone lookup creates/retrieves a `profiles` record with role, display name, and contact metadata.
    - The session and latest profile are cached locally for reopening the app offline.

2. **Route setup**
    - The app reads routes, stops, and schedules from Supabase, caches them in Room, then saves the selected primary route, pickup stop, and destination in `passenger_preferences`.
    - When offline, the passenger can browse cached routes and change the selection; the pending preference update syncs after reconnection.

3. **Live tracker**
    - Driver location updates create `vehicle_locations` records and update the active `trips` state.
    - Supabase Realtime publishes these updates to passengers following that route.
    - The passenger map displays the current position, ETA estimate, and honest last-updated timestamp; stale cached data is labeled accordingly.

4. **Boarding intent**
    - `Konfirmasi akan naik` inserts one `boarding_intents` record for the passenger, active trip, and selected pickup stop.
    - The record uses an idempotency key so retries cannot duplicate the same intent.
    - If offline, Room stores the request in an outbox; WorkManager syncs it to Supabase when connectivity returns.
    - The driver receives the updated waiting-passenger count through Realtime.

5. **Driver trip**
    - Starting a trip updates `trips.status` to active and starts foreground GPS sharing.
    - Marking a stop complete updates the trip’s current stop sequence and status.
    - Finishing the trip writes its end time/status, stops location publication, and returns the passenger dashboard to an unavailable or next-schedule state.

6. **Announcements and notifications**
    - Route-wide/general announcements are stored in `announcements`.
    - Registered FCM device tokens and preferences are stored in `notification_subscriptions`.
    - Notifications deep-link passengers to the relevant live tracker or announcement detail.

## Proposal flowchart notation

Use a database cylinder labeled **“Supabase: Auth + Postgres + Realtime”** and connect it to:

- Authentication → `profiles`
- Route selection → `routes`, `stops`, `passenger_preferences`
- Passenger dashboard/live tracker → `trips`, `vehicle_locations`, `announcements`
- Boarding confirmation → `boarding_intents`
- Driver start/progress/finish → `trips`, `vehicle_locations`
- Push notifications → `notification_subscriptions`

Show Room as an optional local cache/outbox beside the app: it reads cached routes/trip state offline, queues passenger changes, and synchronizes to Supabase on reconnect.

## Screens to explain database use

- **Passenger route review:** “Pilihan rute, halte, dan tujuan utama disimpan ke Supabase agar tersedia saat pengguna membuka aplikasi kembali.”
- **Passenger dashboard/live tracker:** “Status perjalanan dan posisi bus diterima real-time dari Supabase; waktu pembaruan terakhir selalu ditampilkan.”
- **Boarding confirmation:** “Konfirmasi minat naik tersimpan di database dan dihitung pada dashboard driver.”
- **Driver active trip:** “Lokasi GPS dan progres halte dikirim ke Supabase selama perjalanan aktif, lalu berhenti saat trip selesai.”

## Data model and verification

- Use `profiles`, `passenger_preferences`, `routes`, `stops`, `vehicles`, `trips`, `vehicle_locations`, `boarding_intents`, `announcements`, and `notification_subscriptions`.
- Verify passenger selection persists after sign-out/reopen; a driver GPS update appears on a subscribed passenger tracker; driver stop progress updates the passenger ETA/status; duplicate intent retries create one record; and offline changes synchronize correctly once online.
