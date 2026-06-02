import type { Metadata, Viewport } from "next";
import "./globals.css";
import { Providers } from "./providers";

export const metadata: Metadata = {
  metadataBase: new URL("https://pennyrush.dev"),
  title: {
    default: "PennyRush",
    template: "%s | PennyRush",
  },
  description:
    "Track spending, import statements, scan receipts, and plan everyday money from one private money hub.",
  applicationName: "PennyRush",
  manifest: "/manifest.webmanifest",
  icons: {
    icon: "/icon.svg",
    shortcut: "/icon.svg",
    apple: "/icon.svg",
  },
  openGraph: {
    title: "PennyRush",
    description:
      "A private money hub for activity, receipt scans, imports, plans, and spending insights.",
    url: "https://pennyrush.dev",
    siteName: "PennyRush",
    type: "website",
  },
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0A0A0A" },
  ],
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
