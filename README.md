# ShuttleSpot

ShuttleSpot is a proposed Android application for UMN campus shuttle operations. It helps passengers see a bus's current location and ETA, lets drivers share trip status, and gives operations staff a clearer view of service updates.

This repository begins as the durable project context for the final-exam application. The implementation will use Kotlin and Jetpack Compose.

## Product scope

- Passenger: choose a route and stop, see live bus status/ETA, indicate intent to board, and receive service notifications.
- Driver: start/end an assigned trip, share location while on duty, update stop progress, and publish an operational announcement.
- Admin: monitor fleet status and publish announcements. Full chat and complex scheduling are later milestones.

See [product requirements](docs/product.md), [architecture](docs/architecture.md), [design rules](docs/design.md), and the [roadmap](docs/roadmap.md).

## Planned setup

The Android Gradle project has not been initialized yet. The planned modules are `app`, `core`, `data`, and `feature/*`. Before the first runnable build, add the Android project, create a Supabase project, configure Maps and Firebase credentials locally, and follow the first incomplete roadmap item.
