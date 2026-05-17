# Database Schema

The canonical schema lives in [0001_init.sql](</home/royalpinto007/Open-Source/PennyRush/supabase/migrations/0001_init.sql>). A Mermaid ER diagram is available in [database-schema.mmd](</home/royalpinto007/Open-Source/PennyRush/docs/database-schema.mmd>).

## Core Entities

- `profiles`: one row per Supabase Auth user.
- `accounts`: bank, wallet, cash, and credit card containers.
- `categories`: per-user category tree, including starter system categories copied for each user.
- `transactions`: normalized money movement rows.
- `budgets`, `goals`, `subscriptions`, `debts`, `investments`: planning and tracking modules.
- `insights`: AI and rules-based insight feed.
- `merchant_categorizations`: learned user-specific merchant mappings.
- `ai_request_log`: user-visible transparency log for AI calls.
- `net_worth_snapshots`: monthly assets minus liabilities history.

## RLS Rule

Every table has RLS enabled. Users can only access rows that belong to `auth.uid()`. `profiles` uses `id = auth.uid()`; all other user-owned tables use `user_id = auth.uid()`.

Service-role Edge Functions may perform scheduled work, but client requests never use service credentials.
