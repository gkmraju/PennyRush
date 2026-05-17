# Pennyrush Android

Kotlin + Jetpack Compose Android Studio scaffold for the primary Pennyrush client.

## Modules

- `app`: app shell and navigation host.
- `core:designsystem`: Material 3 theme, tokens, and shared UI primitives.
- `core:common`: shared models and formatting helpers.
- `core:network`: Supabase client boundary.
- `core:database-cache`: Room cache boundary.
- `core:security`: app lock and privacy settings boundary.
- `feature:*`: vertical feature modules.

## Setup

Create `local.properties` with your Android SDK path. Client-safe Supabase values should be provided through Gradle or a generated config file during the next auth slice.

AI provider keys must never be added to the Android app. AI calls go through Supabase Edge Functions.
