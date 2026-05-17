# Security Policy

Pennyrush handles personal finance data, so security reports are taken seriously.

## Supported Versions

The active `main` branch is supported during early development.

## Reporting A Vulnerability

Use GitHub private vulnerability reporting for `royalpinto007/PennyRush` if it is enabled. If private reporting is not available, open a minimal public issue asking for a secure contact path, without including exploit details, private data, secrets, logs, or account identifiers.

Please include:

- Affected area, such as web, Android, Supabase schema, or Edge Function.
- Impact and likely severity.
- Reproduction steps using fake data only.
- Any relevant package versions or commit references.

## Privacy-Sensitive Areas

Please pay special attention to:

- Uploaded file handling.
- Supabase RLS policies.
- Auth/session handling.
- AI request payload minimization.
- Edge Function secrets.
- Logs and telemetry.

## Non-Negotiables

- Raw uploaded files must not be persisted.
- AI provider keys must stay server-side.
- Users must only be able to access their own data.
- Security fixes should include tests or a concrete verification note where practical.
