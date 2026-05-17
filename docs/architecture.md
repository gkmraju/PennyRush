# Pennyrush Architecture

## High Level

Pennyrush has two clients sharing one Supabase backend:

- Android: Kotlin, Jetpack Compose, Material 3, Room as cache, WorkManager for queued sync.
- Web: Next.js App Router, TypeScript, Tailwind, Supabase SSR/client helpers. This scaffold uses patched Next 16 because current npm advisories mark the requested Next 14 line as vulnerable.
- Backend: Supabase Auth, Postgres with RLS, Realtime, and Edge Functions.

Supabase is the source of truth. Local stores only cache data and hold offline write queues.

## Upload Privacy Flow

1. The user selects a PDF, CSV, or image.
2. The client attempts local parsing first.
3. If local parsing fails, the file is sent to an Edge Function over HTTPS.
4. The function reads bytes in memory, extracts rows, and returns candidate transactions.
5. The raw file is discarded and never persisted.
6. The user reviews candidate rows before inserting transactions.

No Supabase Storage bucket is used by the app.

## AI Flow

Categorization is intentionally hybrid:

1. Local keyword rules.
2. Per-user learned merchant mappings.
3. Server-side AI only for unknown merchants.

Insights use aggregated summaries and avoid raw transaction details where possible. AI keys live only in Edge Function secrets.

## Realtime And Offline

The clients subscribe to transaction, budget, goal, and insight changes. Writes are optimistic and resolved by `updated_at` with last-write-wins semantics. Android uses WorkManager for queued writes; web uses IndexedDB in a later slice.
