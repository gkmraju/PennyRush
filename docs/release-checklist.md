# PennyRush Release Checklist

Use this checklist before shipping a public Android or web release.

## Required Configuration

- Set production Supabase values:
  - Android: `PENNYRUSH_SUPABASE_URL`
  - Android: `PENNYRUSH_SUPABASE_ANON_KEY`
  - Web: `NEXT_PUBLIC_SUPABASE_URL`
  - Web: `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- Configure Google OAuth redirect URLs for:
  - Web callback: `/auth/callback`
  - Android callback: `pennyrush://auth-callback`
- Publish the web app at the domain used in Android policy links, currently `https://pennyrush.dev`.
- Confirm `/privacy` and `/terms` are reachable on the deployed web domain.
- Confirm `/robots.txt`, `/sitemap.xml`, and core security headers are present on the deployed web domain.

## Android Signing

Release builds are signed automatically when these values are present in `android/local.properties` or the environment:

```properties
PENNYRUSH_RELEASE_STORE_FILE=/absolute/path/to/pennyrush-release.jks
PENNYRUSH_RELEASE_STORE_PASSWORD=...
PENNYRUSH_RELEASE_KEY_ALIAS=...
PENNYRUSH_RELEASE_KEY_PASSWORD=...
```

For GitHub Actions release builds, configure these repository secrets:

- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `PENNYRUSH_SUPABASE_URL`
- `PENNYRUSH_SUPABASE_ANON_KEY`
- `PENNYRUSH_RELEASE_KEYSTORE_BASE64`
- `PENNYRUSH_RELEASE_STORE_PASSWORD`
- `PENNYRUSH_RELEASE_KEY_ALIAS`
- `PENNYRUSH_RELEASE_KEY_PASSWORD`

Create `PENNYRUSH_RELEASE_KEYSTORE_BASE64` from the Play signing upload keystore:

```bash
base64 -w 0 pennyrush-release.jks
```

Build commands:

```bash
cd android
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

Expected outputs:

- APK: `android/app/build/outputs/apk/release/`
- AAB: `android/app/build/outputs/bundle/release/`

If signing values are missing, Gradle can still create an unsigned release APK for local verification, but it is not Play Store-ready. GitHub Actions release builds intentionally fail when signing secrets are missing.

## Verification Gates

Run these before tagging or uploading a build:

```bash
source .env.local
npm run release:check-env
npm audit
npm run web:test
npm run web:lint
npm run web:typecheck
npm run web:build
cd android
./gradlew :app:compileDebugKotlin
./gradlew :feature:home:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Manual QA:

- Sign in on Android and web.
- Add a manual entry.
- Import a CSV and verify duplicates are skipped.
- Scan a receipt with camera and image picker.
- Export CSV from Android and web.
- Delete activity only after confirming export works.
- Check light and dark mode on a small Android device.
- Confirm notification permission, budget alerts, and big-spend alert toggles.

## Store Readiness

- Upload a signed AAB to Play Console.
- Add short description and full description from `docs/play-store-listing.md`.
- Add screenshots for Home, Activity, Plan, Insights, Account, import, and scan flows.
- Complete Play Data Safety using `docs/privacy-policy.md`.
- Confirm the app has no ads and no third-party tracking SDKs.
- Confirm support contact and privacy policy URL are real.
