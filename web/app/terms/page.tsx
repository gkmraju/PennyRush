import Link from "next/link";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Terms of Service | PennyRush",
  description: "Terms for using PennyRush.",
};

const sections = [
  {
    title: "Use Of PennyRush",
    body: [
      "PennyRush helps you record, import, scan, categorize, review, export, and delete personal money activity.",
      "You are responsible for the accuracy of the information you add or import. Always review parsed statement or receipt entries before relying on them.",
    ],
  },
  {
    title: "No Financial Advice",
    body: [
      "PennyRush provides organization, summaries, patterns, and planning helpers. It does not provide legal, tax, investment, credit, insurance, or professional financial advice.",
      "Do not make important financial decisions solely from app-generated summaries or insights.",
    ],
  },
  {
    title: "Your Account",
    body: [
      "You are responsible for keeping your sign-in account secure and for activity performed through your account.",
      "If you believe someone else has accessed your account or data, secure your Google account and contact the PennyRush maintainer through the support channel listed with the app release.",
    ],
  },
  {
    title: "Imports And Scans",
    body: [
      "CSV imports and receipt scans may be incomplete or inaccurate because financial files and receipt layouts vary. PennyRush may skip duplicates, infer categories, or mark unknown values for review.",
      "You should keep your own records and verify entries before using them for budgets, reports, reimbursements, taxes, or decisions.",
    ],
  },
  {
    title: "Acceptable Use",
    body: [
      "Do not use PennyRush to upload illegal content, attack the service, scrape private data, bypass access controls, or interfere with other users.",
      "Do not submit data you do not have permission to manage.",
    ],
  },
  {
    title: "Service Changes",
    body: [
      "PennyRush may change features, data flows, integrations, or availability as the product improves.",
      "The app is provided as-is during early releases. Some features may be experimental, especially parsing, OCR, and generated insights.",
    ],
  },
  {
    title: "Liability",
    body: [
      "To the maximum extent allowed by law, PennyRush is not liable for financial loss, incorrect entries, missed alerts, service interruptions, or decisions made from app output.",
      "Your sole remedy for dissatisfaction with PennyRush is to stop using the app and export or delete your activity as needed.",
    ],
  },
];

export default function TermsPage() {
  return (
    <main className="min-h-screen bg-background px-5 py-8 text-foreground sm:px-8">
      <article className="mx-auto max-w-3xl">
        <Link className="text-sm font-semibold text-muted-foreground hover:text-foreground" href="/">
          Back to PennyRush
        </Link>
        <header className="mt-8 border-b border-border pb-8">
          <p className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">PennyRush</p>
          <h1 className="mt-3 text-4xl font-bold tracking-normal">Terms of Service</h1>
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
