# Full rebuild plan

## Goal

Evolve the UI-first BEG-ShuttleSpot foundation into a role-based campus-bus tracker with verified student accounts, administrator-provisioned drivers, realtime bus positions, offline data, and trip-critical notifications.

## Product decisions

- Students use Supabase email/password accounts. Entry requires a verified email ending exactly in `student.umn.ac.id`; Resend SMTP delivers confirmations and password resets.
- Drivers use an administrator-provisioned Supabase phone/password account. The first sign-in replaces the temporary administrator-issued passcode with a private permanent passcode.
- The administrator uses Supabase Dashboard and documented seed/provisioning tools; no third in-app admin role is part of the first release.
- MapLibre renders the native map; OpenFreeMap supplies the configurable hosted base-map style. Cached route/stops provide the offline fallback.
- A driver has one current/next assigned trip. Active foreground tracking reports meaningful movement roughly every 10–15 seconds or 15 metres.
- Passenger boarding confirmation is a one-way, idempotent demand signal, not a seat reservation.

## Backend milestones

1. Add Room cache/outbox and replace in-memory repositories behind the existing interfaces.
2. Create Supabase schema, row-level security, route/stop/geometry seeds, profiles, preferences, trips, locations, boarding intents, announcements, and notification subscriptions.
3. Configure student email confirmation with Resend SMTP and implement password reset/deep links.
4. Add administrator driver provisioning, assigned-trip queries, and secure trip/location RPCs.
5. Integrate MapLibre/OpenFreeMap with cached route geometry, live bus markers, and last-updated/stale states.
6. Add a location foreground service for active drivers and bounded offline recovery for unsent positions/stop progress.
7. Configure FCM for announcements, trip start/end, and bus-near/bus-at-pickup notifications with deep links.

## Security and offline rules

- Android contains only the Supabase URL and anon/publishable key. Service-role, Resend, and Firebase server credentials stay server-side.
- Passengers can access public active trip data plus their own preferences/intents. Drivers can access only their assigned trip and aggregate stop demand.
- Room retains routes, stops, geometry, primary selection, announcements, latest trip snapshot, and a bounded mutation outbox.
- During driver connection loss, the app keeps active-trip data locally and tells the driver to reconnect. Passenger data remains visibly stale until cloud updates resume.
- Store the latest location for each active trip and retain sparse history for 24 hours only.

## Verification

- Student domain, confirmation, permission, manual-route, invalid-stop-order, and saved-route flows.
- Driver phone/passcode, first-login replacement, no-assignment, denied-location, start/progress/finish, and reconnect states.
- RLS role isolation, duplicate intent protection, Realtime passenger updates, FCM deep links, and 24-hour location cleanup.
