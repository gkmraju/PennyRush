import type { MetadataRoute } from "next";

const siteUrl = "https://pennyrush.dev";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: ["/", "/privacy", "/terms", "/manifest.webmanifest", "/icon.svg"],
      disallow: ["/auth"],
    },
    sitemap: `${siteUrl}/sitemap.xml`,
    host: siteUrl,
  };
}
