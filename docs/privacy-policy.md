# Pennyrush Privacy Policy

Last updated: May 17, 2026

Pennyrush is designed to help you track personal finances without turning your financial life into an advertising product.

## Data We Store

Pennyrush stores the structured finance data you choose to save, including accounts, categories, transactions, budgets, goals, subscriptions, debts, investments, insights, preferences, and AI activity logs.

## Files Are Never Stored

When you import a bank statement, CSV, PDF, or receipt image, Pennyrush processes the file in memory. The raw file is discarded immediately after parsing. Pennyrush does not upload files to Supabase Storage or any other object storage service in v1.

Only the extracted transaction data that you confirm is saved.

## AI Processing

AI features run through Supabase Edge Functions. Model API keys are never included in the Android or web clients.

Pennyrush minimizes AI payloads. Categorization requests may include merchant text, amount, and notes for unknown transactions. Insight requests use compact aggregate summaries where possible. Pennyrush never sends account numbers, user names, emails, or authentication identifiers to AI providers.

You can disable AI features with local-only mode.

## Security

Pennyrush uses Supabase Auth and Postgres Row Level Security so users can only access their own rows. All requests use HTTPS. Android app lock and web two-factor authentication are part of the security roadmap.

## Account Deletion

Deleting your account cascades through user-owned Pennyrush data. Raw uploaded files do not need deletion because they are never stored.

## Tracking And Ads

Pennyrush v1 uses no advertising SDKs, no paid analytics SDKs, and no third-party tracking pixels.
