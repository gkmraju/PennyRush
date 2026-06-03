# PennyRush Play Data Safety Notes

Use these notes to complete the Google Play Data Safety form for the v1 Android release. They describe the current app implementation and must be rechecked before each public release.

## App Category

- App type: Personal finance
- Ads: No
- Third-party tracking SDKs: No
- Account creation: Yes, through Google sign-in backed by Supabase Auth
- Account deletion: Yes, available in Android and web Account controls
- Data encryption in transit: Yes, production traffic must use HTTPS

## Data Collected

PennyRush collects only data the user signs in with or chooses to save.

| Data Type | Collected | Purpose | Shared |
| --- | --- | --- | --- |
| Name | Optional | Account display and personalization | No sale or advertising sharing |
| Email address | Yes | Sign-in and account identification | No sale or advertising sharing |
| User IDs | Yes | Authenticated app records | No sale or advertising sharing |
| Financial info | Yes | Personal finance tracking, budgets, goals, insights, imports, exports | No sale or advertising sharing |
| Photos or videos | Optional, transient | Receipt scan review from camera or image picker | No sale or advertising sharing |
| App interactions | Yes, app records only | Saved activity, preferences, budgets, goals, and insights | No sale or advertising sharing |

## Financial Data Details

Saved finance data can include:

- Accounts
- Categories
- Activity entries
- Transaction dates, amounts, type, merchant, and notes
- Budgets
- Goals
- Insights derived from saved activity
- User preferences such as currency, theme, and alert settings

## Receipt And Statement Handling

- Web CSV imports are parsed in the browser.
- Raw CSV statement files are not uploaded to storage by the web import flow.
- Android receipt images are read for review and entry extraction.
- Android receipt images are not stored by PennyRush after scan review.
- PennyRush saves the confirmed activity entry, not the raw receipt image.
- Camera captures are stored in app cache only for the scan/review flow.

## Sharing

PennyRush uses Supabase for authentication and app data storage. Do not mark data as sold or shared for advertising. If a future release adds analytics, crash reporting, server-side AI processing, or another third-party processor, update this document, the privacy policy, and the Play Data Safety form before release.

## User Controls

Users can:

- Export activity as CSV.
- Delete saved activity.
- Delete their PennyRush account and saved app data.
- Turn notification-based budget and spending alerts on or off.

## Permission Notes

Current Android permissions:

- `INTERNET` for authentication and app data access.
- `POST_NOTIFICATIONS` for budget and spending alerts.

Android app lock uses the device biometric or screen-lock prompt. PennyRush does not receive or store fingerprint, face, PIN, pattern, or password data.

The current app does not request SMS, contacts, location, microphone, calendar, or broad photo library permissions.
