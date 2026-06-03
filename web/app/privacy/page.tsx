import Link from "next/link";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Privacy Policy | PennyRush",
  description: "How PennyRush handles account, activity, import, and receipt data.",
};

const sections = [
  {
    title: "What PennyRush Collects",
    body: [
      "When you sign in, PennyRush stores your account profile, accounts, categories, budgets, goals, insights, and activity entries so the app can show your money hub across devices.",
      "Activity entries can come from manual entry, CSV statement import, receipt scanning, or future review flows. Entries may include date, amount, merchant, category, notes, source, and import identifiers used to avoid duplicates.",
    ],
  },
  {
    title: "Statement And Receipt Files",
    body: [
      "The web import flow parses statement files in the browser and does not upload the raw statement file to storage.",
      "Android receipt scanning reads the selected or captured image to extract text for an entry. PennyRush saves the resulting activity entry, not the raw receipt image, unless you separately keep that image on your device.",
    ],
  },
  {
    title: "How Data Is Used",
    body: [
      "PennyRush uses your activity to calculate balances, spending patterns, budget progress, recent merchants, exports, and useful insights.",
      "PennyRush does not sell your personal financial activity. The app is built to help you understand your own money, not to broker ads from your activity data.",
    ],
  },
  {
    title: "Accounts And Service Providers",
    body: [
      "PennyRush uses Google sign-in for authentication and Supabase for app data storage, authentication session handling, and database access.",
      "These services process data needed to run the app. Their own terms and privacy policies also apply when you use those services through PennyRush.",
    ],
  },
  {
    title: "Your Controls",
    body: [
      "You can export activity as a CSV file from the Account and data section.",
      "You can delete all saved activity entries from the app. Deleting activity removes the entries used by the dashboard, activity list, imports, and insights.",
      "You can also delete your PennyRush account from the web or Android Account and data tools. Account deletion removes your PennyRush authentication record and saved app data, but it does not delete your Google account.",
    ],
  },
  {
    title: "Security",
    body: [
      "PennyRush limits raw file handling, uses authenticated database access, and avoids storing raw statement files in the web import flow.",
      "No financial app can guarantee perfect security. Use a strong Google account password, keep your device updated, and report anything suspicious.",
    ],
  },
  {
    title: "Contact",
    body: [
      "For privacy questions, data requests, or security reports, contact the PennyRush maintainer at github.com/royalpinto007/PennyRush/issues.",
    ],
  },
];

export default function PrivacyPage() {
  return (
    <main className="min-h-screen bg-background px-5 py-8 text-foreground sm:px-8">
      <article className="mx-auto max-w-3xl">
        <Link className="text-sm font-semibold text-muted-foreground hover:text-foreground" href="/">
          Back to PennyRush
        </Link>
        <header className="mt-8 border-b border-border pb-8">
          <p className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">PennyRush</p>
          <h1 className="mt-3 text-4xl font-bold tracking-normal">Privacy Policy</h1>
          <p className="mt-4 text-sm leading-6 text-muted-foreground">Last updated: June 3, 2026</p>
        </header>
        <div className="space-y-8 py-8">
          {sections.map((section) => (
            <section key={section.title}>
              <h2 className="text-xl font-bold">{section.title}</h2>
              <div className="mt-3 space-y-3 text-sm leading-7 text-muted-foreground">
                {section.body.map((item) => (
                  <p key={item}>{item}</p>
                ))}
              </div>
            </section>
          ))}
        </div>
      </article>
    </main>
  );
}
