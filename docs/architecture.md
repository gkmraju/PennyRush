# PennyRush Architecture

## High Level

PennyRush has two clients sharing one Supabase backend:

- Android: Kotlin, Jetpack Compose, Material 3, local in-memory activity state, and Supabase-backed persistence when configured.
- Web: Next.js App Router, TypeScript, Tailwind, Supabase SSR/client helpers, CSV import, manual entries, export, and account controls.
- Backend: Supabase Auth, Postgres with RLS, Realtime, and Edge Functions.

Supabase is the source of truth for authenticated production use. Local client state exists to keep screens responsive and to support review flows before entries are saved.

## Upload Privacy Flow

1. The user selects a CSV statement or receipt image.
2. The client attempts local parsing first.
3. The app extracts candidate entries in memory.
4. The raw file is discarded by the client flow and never persisted to Supabase Storage.
5. The user reviews candidate rows before inserting transactions.

No Supabase Storage bucket is used by the app.

## Categorization And Insights

Current categorization is rules-based:

1. Local keyword rules.
2. Merchant extraction.
3. Saved category data when available.

Insights use saved activity summaries and avoid raw file handling. If server-side AI is enabled later, provider keys must live only in server-side secrets and the privacy policy must be updated before release.

## Realtime And Reliability

The web app subscribes to transaction changes and refreshes after imports. Android loads and persists entries through the app-level Supabase wiring when configured. Durable offline queues, conflict resolution, and background sync are future hardening items rather than current release guarantees.
