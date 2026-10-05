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

1. A passenger enters a valid UMN student email in the form `name@student.umn.ac.id`; the app grants passenger access only when the email domain is exactly `student.umn.ac.id`. They grant location permission only if they use nearby-stop assistance.
2. They select a primary route, pickup stop, and destination, then can inspect the route stops and departure schedule.
3. Home displays the next active bus, its route, live position/ETA, and service status.
4. The passenger can signal that they intend to board at the selected stop.
5. The app notifies them when the bus is near or has arrived; they can read service announcements.

## Core flow: driver MVP

1. The driver enters their registered phone number. The app grants demo driver access only when its normalized value matches a pre-registered driver profile, then opens the assigned trip. This lookup is not phone-number ownership verification.
2. They start the trip; while actively driving, the app publishes location and trip status with a visible foreground-service notification.
3. At each stop they confirm progress and can see the count of passenger boarding intents.
4. They end the trip and location sharing stops.

## Must-have (final-exam MVP)

- Role-aware sign-in: passenger access uses a valid `name@student.umn.ac.id` email-domain gate; driver access uses a registered-phone lookup against pre-registered driver profiles. Both are demo eligibility checks, not identity verification or proof that the user owns the supplied contact detail.
- Route, stop, and departure-schedule browsing; a passenger can save one primary route, pickup stop, and destination.
- Live map/status card for an active bus; show an honest `last updated` timestamp and unavailable state when no live data exists.
- Driver trip controls: start, stop progress, finish trip, and publish location when a trip is active.
- Passenger boarding-intent record for a selected active trip and stop.
- Announcements targeted to all users or a route.
- Push notifications for nearby/arrived bus and operational announcements.
- Supabase as the cloud system of record.
- Offline-first cache for routes, stops, latest trip state, announcements, and queued user actions; sync automatically on reconnect.

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
