# Detailed ShuttleSpot passenger, driver, and Supabase flow

## Summary

Deliver a role-based Android demo with Mahasiswa and Driver only. Mahasiswa selects a route and tracks a bus; Driver starts an assigned trip and shares live location. Supabase is the online system of record, while Room provides offline route/trip cache and an outbox for retryable passenger actions.

Passenger access remains an `@student.umn.ac.id` email-domain gate. Driver access is a lookup of a pre-registered phone number in Supabase—there is no OTP. Both are demo eligibility checks, not secure identity verification.

## Screen-by-screen flow

| Screen | User action and content | Data and next step |
| --- | --- | --- |
| 1. Welcome | Show ShuttleSpot purpose and visible `Masuk` button. | `Masuk` opens Role Selection. |
| 2. Role Selection | Show only `Mahasiswa` and `Driver`; do not show staff. | Persist the selected role temporarily and open the matching sign-in screen. |
| 3. Mahasiswa Sign-in | One email field; validate syntax and exact `student.umn.ac.id` domain. Show inline error for invalid input. | Create/retrieve a demo passenger profile and session, then open Location Assistance. |
| 4. Location Assistance | Explain that location is used only to recommend nearby stops. Provide `Aktifkan Lokasi` and `Pilih Manual`. | Request runtime permission only after the first action. Allowed opens Nearby Stops; denied or skipped opens Manual Stop Selection. |
| 5. Nearby Stops | Show current position and ranked nearby stops on list/map. Passenger selects pickup stop, then destination. | Device location is used only for ranking; it is not stored as passenger tracking data. Continue to Route Results. |
| 6. Manual Stop Selection | Searchable pickup-stop field, map picker, destination selector, and route direction. | Validate that both stops belong to a route and the pickup comes before the destination for that direction. Continue to Route Results. |
| 7. Route Results | List recommended routes first. Each route card shows direction, operating hours, next departures, pickup/destination, and `Lihat Rute`. | Passenger selects one route to inspect Route Detail. |
| 8. Route Detail | Map route line, ordered stop list, direction tabs, available departure times, and selected pickup/destination. | `Jadikan rute utama` opens Review; back returns to Route Results without saving. |
| 9. Review Selection | Show passenger email, primary route, pickup stop, destination, and schedule summary. | `Sudah benar` saves `passenger_preferences`; success opens Registration Complete. |
| 10. Registration Complete | Confirm that the route is ready, with `Ke Beranda`. | Enters Passenger Home. |
| 11. Passenger Home: no active bus | Show route/pickup summary, next scheduled departure, announcement preview, and explicit `Tidak ada perjalanan aktif` state. | Route tab lets passenger change selection; notifications and announcements remain available. |
| 12. Passenger Home: active bus | Show bus plate/identifier, route progress, next/pickup stop, ETA estimate, status, and last-updated time. Actions: `Konfirmasi akan naik` and `Lihat Posisi Bus Realtime`. | Boarding action opens Confirmation; tracker opens Live Tracker. |
| 13. Boarding Confirmation | Explain that confirmation means boarding intent, not verified check-in. Allow confirm or cancel before the bus passes the pickup stop. | Insert/update one `boarding_intents` record. Show confirmed, pending-sync, cancelled, or unavailable-after-passed state. |
| 14. Live Tracker | Show live bus marker, route line, stops, pickup/destination pins, status, ETA, route-progress indicator, and last update. The bottom sheet repeats boarding-intent status/action. | Realtime updates refresh map/status. Stale location stays visible with timestamp; unavailable state explains there is no live feed. |
| 15. Passenger Route tab | Display current primary route plus searchable alternate routes/stops. Changing the primary selection requires confirmation. | Writes the new `passenger_preferences` record and updates Home. |
| 16. Announcements | Display route-wide/general operational messages separately from personal notifications. | Open announcement detail; route announcements may trigger FCM notifications. |
| 17. Notifications | Display personal trip alerts such as “bus nearby” or “bus has arrived.” | Selecting an item deep-links to the matching Live Tracker or announcement detail. |
| 18. Profile | Show passenger profile, selected route, notification preferences, and sign-out. | Preferences update `notification_subscriptions`; sign-out clears the local session but retains permitted cache. |
| 19. Driver Sign-in | Accept a phone number, normalize it to the stored Indonesian `+62…` form, and look up a profile with the Driver role. | Registered number opens Driver Home. Unknown number shows “Nomor belum terdaftar, hubungi koordinator” without exposing database details. |
| 20. Driver Home: before trip | Show driver, vehicle, assigned route, shift, trip number, complete stop list, and passenger-intent count by upcoming stop. | `Mulai perjalanan` opens a confirmation dialog. No assigned trip shows a neutral empty state and no location action. |
| 21. Start Trip confirmation | Explain that starting publishes GPS only while this trip is active. | On confirmation, request driver location permission if needed. If allowed, set `trips.status = active`, start foreground location service, and open Driver Active Trip. If denied, keep the trip inactive and provide retry/settings actions. |
| 22. Driver Active Trip | Show driver position, route line, current/next stop, active trip status, passenger-intent count for the next stop, and large `Tiba di halte ini` / `Lanjut ke halte berikutnya` controls. | GPS updates publish location. Each explicit stop confirmation advances `current_stop_sequence`; no automatic geofence progression is used. |
| 23. Finish Trip confirmation | Available at the final stop; require confirmation before ending. | Set trip completed/end timestamp, stop foreground location service, stop location publication, and show Trip Complete. |
| 24. Trip Complete | Show completed route, vehicle, trip number, and completion status. | `Kembali ke beranda` returns to Driver Home. Passenger Home changes from active-trip tracking to next-schedule/no-active-bus state. |

## Supabase, realtime, and offline behavior

- `profiles` stores role, name, and normalized phone/email metadata. `passenger_preferences` stores one passenger’s primary route, pickup, and destination.
- `routes`, `stops`, `vehicles`, and `trips` supply static and assigned-trip data. `vehicle_locations` stores timestamped driver GPS updates. `boarding_intents`, `announcements`, and `notification_subscriptions` support the remaining flows.
- Driver start/progress/finish writes update `trips`; active driver GPS writes create `vehicle_locations` records. Supabase Realtime publishes relevant active-trip, location, and intent-count changes to passenger and driver screens.
- Passenger ETA is a clearly labeled estimate calculated from current route progress plus average segment/stop time. It always includes `Data terakhir diperbarui`.
- Room caches routes, stops, saved selection, announcements, and latest trip snapshot. Passenger selection updates and boarding intents enter a WorkManager outbox when offline, carrying an idempotency key.
- Passenger screens must show cached/stale state when offline. Driver trip start requires connectivity; if connectivity drops during an active trip, preserve the active state, cache unsent updates, label passenger information as stale, and resume sync when reconnected.
- Supabase RLS permits passengers to manage only their own profile/preferences/intents and read active public route data; drivers can update only their assigned active trip and location.

## Interfaces and acceptance tests

- Add role-aware session, saved route selection, live trip snapshot, ETA state, boarding-intent state, and driver trip-progress state holders exposed through ViewModels.
- Repository operations include profile lookup, route/stop discovery, save primary selection, observe active trip, submit/cancel boarding intent, start/progress/finish driver trip, and publish location.
- Test the complete passenger journey: invalid email, location allowed/denied/manual fallback, invalid stop order, route saving, no-bus/active/stale/missed/arrived states, duplicate boarding retries, offline intent queueing, and reconnect synchronization.
- Test the complete driver journey: registered/unregistered phone lookup, no assigned trip, denied location permission, start-trip confirmation, active GPS publication, manual stop progression, connectivity loss/recovery, finish-trip confirmation, and stopped location publishing.
- Verify a driver location update appears on a selected passenger tracker, the waiting-intent count updates on the driver screen, and all sensitive actions remain unavailable outside the correct role/trip state.

## Assumptions

- Use labeled demo routes, schedules, drivers, vehicles, and phone numbers until UMN operational data is approved.
- The driver phone lookup is suitable only for the course demo; a production version must add real authentication.
- Staff/admin, payments, ticketing, automated passenger detection, automatic stop progression, and free-form driver chat are outside this MVP.
