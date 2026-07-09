import type { MetadataRoute } from "next";

const siteUrl = "https://pennyrush.agentpostmortem.workers.dev";

export default function sitemap(): MetadataRoute.Sitemap {
  const lastModified = new Date("2026-06-03");

  return [
    {
      url: siteUrl,
      lastModified,
      changeFrequency: "weekly",
      priority: 1,
    },
    {
      url: `${siteUrl}/privacy`,
      lastModified,
      changeFrequency: "monthly",
      priority: 0.6,
    },
    {
      url: `${siteUrl}/terms`,
      lastModified,
      changeFrequency: "monthly",
      priority: 0.6,
    },
  ];
}
