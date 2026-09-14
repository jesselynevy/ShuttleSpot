# ADR 0001: Kotlin/Compose with Supabase and Room

## Status

Accepted — initial project decision.

## Context

The final-exam app must be a Kotlin/Jetpack Compose mobile application, persist user data online, and benefits from working without a connection.

## Decision

Use Kotlin, Jetpack Compose, Supabase (Auth/Postgres/Realtime), and Room plus WorkManager. Supabase is authoritative; Room is a local cache and an outbox for retryable user mutations.

## Consequences

- The project can demonstrate both an online database and offline-first behaviour.
- The app needs clear sync-state UI, RLS policies, and idempotent write design.
- Schema changes must update `docs/architecture.md` and this decision record if they change the rationale.
