# PennyRush

A private money hub for activity, receipt scans, imports, plans, and spending insights.

PennyRush is a privacy-first personal finance tracker with a native Android app, a companion web app, and one Supabase backend. Statement files and receipt images are processed for extraction; raw files are not uploaded to app storage or kept by the clients. Only saved activity fields such as amount, date, merchant, note, type, and category are stored.

Repository: https://github.com/royalpinto007/PennyRush

## What Is In This Repo

- `web/`: Next.js App Router companion app with authentication, activity dashboard, manual entries, CSV import, privacy/terms pages, and export/delete controls.
- `android/`: Kotlin + Jetpack Compose app with Home, Activity, Plan, Insights, Account, manual entry, import, receipt scan, budgets/goals, alerts, export, and delete controls.
- `shared/`: Shared TypeScript contract types for web and Supabase functions.
- `supabase/`: Local-only schema/functions workspace. It is intentionally ignored so linked project state and secrets are not pushed.
- `docs/`: Architecture, database, privacy, Play Store listing, and release checklist material.

## Local Setup

```bash
npm install
npm run web:dev
```

Create `.env.local` from `.env.example`:

```bash
NEXT_PUBLIC_SUPABASE_URL=...
NEXT_PUBLIC_SUPABASE_ANON_KEY=...
PENNYRUSH_SUPABASE_URL=...
PENNYRUSH_SUPABASE_ANON_KEY=...
```

Android local build:

```bash
cd android
./gradlew :app:assembleDebug
```

Release verification:

```bash
npm audit
npm run web:test
npm run web:lint
npm run web:typecheck
npm run web:build
cd android
./gradlew :app:compileDebugKotlin
./gradlew :feature:home:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

Supabase service-role and AI provider secrets must be configured only in secure server-side environments, never in client apps:

```bash
supabase secrets set GROQ_API_KEY=...
supabase secrets set GEMINI_API_KEY=...
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=...
```

## Privacy Contract

PennyRush does not use Supabase Storage in v1. Files selected for statement import or receipt scan are:

1. Read into memory.
2. Parsed into structured candidate transactions.
3. Discarded by the client flow after extraction.
4. Never written to disk, object storage, analytics, or logs.

Current categorization and insight helpers are rules-based. If server-side AI is enabled later, clients must never receive model API keys and payloads must stay minimized.

## Release

Use [docs/release-checklist.md](docs/release-checklist.md) before shipping. GitHub Actions verifies web and Android changes on push/PR and can build release artifacts when production Supabase and Android signing secrets are configured.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md), [SECURITY.md](SECURITY.md), and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

PennyRush is licensed under the [MIT License](LICENSE).
