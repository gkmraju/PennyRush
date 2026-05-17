# Pennyrush

Every penny, in a rush to be tracked.

Pennyrush is a privacy-first personal finance tracker with a native Android app, a companion web app, and one Supabase backend. Uploaded bank statements and receipts are parsed in memory and discarded immediately. Only extracted transaction data is stored.

Repository: https://github.com/royalpinto007/PennyRush

## What Is In This Repo

- `web/`: Next.js App Router app with Tailwind, shadcn-style primitives, Supabase clients, import parsing utilities, and the first dashboard experience. The scaffold uses a patched Next 16 release because current npm advisories mark the Next 14 line as vulnerable.
- `android/`: Kotlin + Jetpack Compose Android Studio scaffold using Material 3, Hilt-ready modules, and feature boundaries.
- `supabase/`: Local-only Postgres schema, RLS policies, seed helper, and Edge Functions for categorization, insights, recurring detection, and parse fallback. This directory is intentionally ignored and should not be pushed.
- `shared/`: Shared TypeScript contract types for the web app and Edge Functions.
- `docs/`: Architecture, schema, privacy, and store listing material.

## Local Setup

```bash
npm install
npm run web:dev
```

Create `web/.env.local` from `web/.env.example`:

```bash
NEXT_PUBLIC_SUPABASE_URL=...
NEXT_PUBLIC_SUPABASE_ANON_KEY=...
```

Supabase Edge Function secrets must be configured in Supabase, not in client apps:

```bash
supabase secrets set GROQ_API_KEY=...
supabase secrets set GEMINI_API_KEY=...
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=...
```

## Privacy Contract

Pennyrush does not use Supabase Storage in v1. Files uploaded for statement import or receipt OCR are:

1. Read into memory.
2. Parsed into structured candidate transactions.
3. Discarded before the response finishes.
4. Never written to disk, object storage, analytics, or logs.

AI requests are server-side only. The clients never receive model API keys. AI payloads are minimized and logged in `ai_request_log` so users can audit activity.

## Build Order

This scaffold starts the Week 1 to Week 3 foundation: schema, RLS, client shells, design language, transaction primitives, and private import flow. See [docs/week-plan.md](</home/royalpinto007/Open-Source/PennyRush/docs/week-plan.md>) for the staged roadmap.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md), [SECURITY.md](SECURITY.md), and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

Pennyrush is licensed under the [MIT License](LICENSE).
