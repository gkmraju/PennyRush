# PennyRush Production Deployment

This runbook covers the external steps required to ship PennyRush to users. The repository can verify builds and metadata, but these account-level actions must be completed in GitHub, Vercel, Supabase, and Google Play Console.

## 1. Production Accounts

- GitHub repository: `royalpinto007/PennyRush`
- Production web host: Vercel project for `https://pennyrush.dev`
- Production database/auth: Supabase project used only for public release
- Android distribution: Google Play Console app for package `dev.pennyrush.app`
- Support channel: `https://github.com/royalpinto007/PennyRush/issues`

## 2. Supabase

- Apply the current schema from `docs/database-schema.md` to the production Supabase project.
- Enable email or OAuth sign-in providers intended for launch.
- Configure redirect URLs:
  - Web: `https://pennyrush.dev/auth/callback`
  - Android: `pennyrush://auth-callback`
- Confirm row-level security is enabled for user-owned finance tables.
- Create a production service role key only for trusted server-side account deletion.
- Test with a throwaway account:
  - Sign in on web.
  - Sign in on Android.
  - Add an entry.
  - Delete the account.
  - Confirm the deleted account cannot access old activity.

## 3. Vercel

- Connect the repository and deploy the web app from `web`.
- Set production environment variables:
  - `NEXT_PUBLIC_SUPABASE_URL`
  - `NEXT_PUBLIC_SUPABASE_ANON_KEY`
  - `SUPABASE_SERVICE_ROLE_KEY`
- Assign `https://pennyrush.dev` as the production domain.
- Verify these public URLs:
  - `https://pennyrush.dev/privacy`
  - `https://pennyrush.dev/terms`
  - `https://pennyrush.dev/robots.txt`
  - `https://pennyrush.dev/sitemap.xml`
- Verify response headers include HSTS, frame protection, content type protection, referrer policy, and permissions policy.

## 4. GitHub Actions secrets

Configure these repository secrets before running the release workflow:

- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `PENNYRUSH_SUPABASE_URL`
- `PENNYRUSH_SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `PENNYRUSH_RELEASE_KEYSTORE_BASE64`
- `PENNYRUSH_RELEASE_STORE_PASSWORD`
- `PENNYRUSH_RELEASE_KEY_ALIAS`
- `PENNYRUSH_RELEASE_KEY_PASSWORD`

Generate the keystore secret from the Play upload keystore:

```bash
base64 -w 0 pennyrush-release.jks
```

## 5. Local Verification

Run this with production-like values loaded:

```bash
source .env.local
npm run release:check-env
npm run release:audit-config
npm run release:audit-store
npm run release:verify
```

`npm run release:verify` builds the web app, runs web checks, runs Android Kotlin compile, release lint, unit tests, and creates APK/AAB artifacts.

## 6. Signed AAB

- Run the GitHub release workflow after secrets are configured.
- Download the `pennyrush-release-aab` artifact.
- Confirm the artifact is signed with the Play upload key.
- Upload the signed AAB to an internal Play testing track first.

## 7. Google Play Console

- App package: `dev.pennyrush.app`
- App name: `PennyRush`
- Privacy policy URL: `https://pennyrush.dev/privacy`
- Listing copy: `docs/play-store-listing.md`
- Fastlane metadata: `android/fastlane/metadata/android/en-US/`
- Data Safety answers: `docs/play-data-safety.md`
- Required screenshots:
  - Home
  - Activity
  - Plan
  - Insights
  - Account
  - Add entry
  - Import
  - Scan receipt

## 8. Production smoke test

Complete this after installing from Play internal testing:

- Sign in.
- Add a manual entry.
- Import a CSV and confirm duplicate detection on re-import.
- Scan a receipt from camera and image picker.
- Create a budget and goal.
- Confirm budget and big-spend alerts.
- Export activity as CSV.
- Delete activity after export.
- Delete a throwaway account.
- Check light and dark mode on a small phone.
- Check web dashboard with the same account.

## 9. Launch Decision

Ship only when all of these are true:

- `npm run release:verify` passes.
- Production web URLs and legal pages are reachable.
- Signed AAB is accepted by Play internal testing.
- Play Data Safety matches the actual app behavior.
- Manual smoke test passes on Android and web.
- Support channel is live and monitored.
