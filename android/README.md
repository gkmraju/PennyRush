# PennyRush Android

Kotlin + Jetpack Compose Android app for the primary PennyRush client.

## Modules

- `app`: app shell and navigation host.
- `core:designsystem`: Material 3 theme, tokens, and shared UI primitives.
- `core:common`: shared models and formatting helpers.
- `core:network`: Supabase client boundary.
- `core:database-cache`: Room cache boundary.
- `core:security`: app lock and privacy settings boundary.
- `feature:*`: vertical feature modules.

## Setup

Create `local.properties` with your Android SDK path. Client-safe Supabase values can be provided in `local.properties` or environment variables:

```properties
PENNYRUSH_SUPABASE_URL=...
PENNYRUSH_SUPABASE_ANON_KEY=...
```

Build and verify:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :feature:home:testDebugUnitTest :app:assembleDebug
./gradlew :app:assembleRelease :app:bundleRelease
```

Release signing is optional for local verification and required for Play Store upload:

```properties
PENNYRUSH_RELEASE_STORE_FILE=/absolute/path/to/pennyrush-release.jks
PENNYRUSH_RELEASE_STORE_PASSWORD=...
PENNYRUSH_RELEASE_KEY_ALIAS=...
PENNYRUSH_RELEASE_KEY_PASSWORD=...
```

AI provider keys must never be added to the Android app.
