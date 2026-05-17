# Contributing To Pennyrush

Thanks for helping make Pennyrush better. This project is privacy-first personal finance software, so contributions should preserve trust before chasing convenience.

## Repository

Primary repository: https://github.com/royalpinto007/PennyRush

## Development Setup

```bash
npm install
npm run web:dev
```

Useful checks:

```bash
npm run web:typecheck
npm run web:lint
npm run web:build
```

Android currently uses the scaffold in `android/`. Run Android checks from Android Studio or a local Gradle install once the Gradle wrapper is added.

## Privacy Rules

- Do not persist uploaded bank statements, receipt images, PDFs, or CSV files.
- Do not add Supabase Storage usage for imported files.
- Do not put AI provider keys in Android or web client code.
- Do not log raw financial files, account numbers, emails, auth tokens, or complete transaction exports.
- Keep AI payloads minimal and routed through server-side functions.
- Keep RLS policies on every user-owned table.

## Branches And Commits

Use small branches with clear names:

- `feature/import-preview`
- `fix/rls-category-ownership`
- `docs/privacy-policy`

Write commits in plain language and keep unrelated changes separate.

## Pull Requests

Before opening a PR:

1. Run the relevant checks.
2. Update docs for behavior or privacy changes.
3. Add screenshots for UI work.
4. Explain any schema or auth changes.
5. Call out any security or migration risk.

## Design Contributions

Pennyrush should feel clean, minimal, and premium. Prefer whitespace, clear typography, one primary action per screen, accessible contrast, and predictable workflows.

## Dependency Policy

The v1 stack should stay free to run at small scale. Avoid paid SDKs, ad SDKs, tracking SDKs, and dependencies that require hosted paid services for core functionality.
