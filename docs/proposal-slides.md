 # Proposal slide outline

Use this as the content source for the lecturer-required presentation. Keep slide copy brief; cite the project research report for interview/observation findings and label external claims with their sources.

1. **Title — ShuttleSpot**: Real-time campus-shuttle information for UMN.
2. **Problem statement**: Passengers lack reliable real-time bus position/ETA; schedules and updates are still often manual. Drivers lack visibility of waiting demand; staff answer repeated status questions.
3. **Evidence and impact**: Prototype research included interviews with two active riders and one intern, observation, and testing. Reported effects: uncertain waiting time, missed buses, late arrival risk, operational inefficiency.
4. **Target users**: Passenger, driver, and operations staff — one short problem/need per role.
5. **Proposed solution**: A role-based Kotlin/Jetpack Compose Android application linking live driver trip updates to passenger tracking and operations announcements.
6. **Key features**: Route/stop selection, live bus map and ETA, boarding intent, driver trip controls, announcements, and alerts.
7. **Native device features**: GPS location for active driver trips; optional nearby-stop assistance for passengers; FCM push notifications. Include privacy: driver location is active only while a trip is running.
8. **Data storage and offline-first**: Supabase cloud database/realtime as source of truth; Room cache and queued actions; WorkManager sync when online.
9. **Core user flow**: Passenger selects route/stop → tracks bus → signals intent → receives near-arrival alert. Driver starts trip → shares location → progresses stops → finishes trip.
10. **UI mockups (1/2)**: Screens 1–6 from `docs/design.md`.
11. **UI mockups (2/2)**: Screens 7–12 from `docs/design.md`.
12. **Implementation plan and success criteria**: Show the roadmap's six phases and the product acceptance criteria.

## Suggested narration

`ShuttleSpot does not promise perfect transport prediction. It makes the best known operational state visible, timestamps it honestly, and keeps essential route information usable even when connectivity is interrupted.`
