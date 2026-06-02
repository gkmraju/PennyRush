"use client";

import { AlertTriangle, CheckCircle2, Info } from "lucide-react";
import { motion } from "framer-motion";
import type { InsightRow } from "@/lib/dashboard-data";
import { cn } from "@/lib/utils";

export function InsightsCarousel({ insights }: { insights: InsightRow[] }) {
  return (
    <section aria-label="Insights">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-bold">Insights</h2>
        <button className="text-sm font-semibold text-muted-foreground" type="button">
          See all
        </button>
      </div>
      {insights.length === 0 ? (
        <div className="rounded-card bg-card p-5 text-sm leading-6 text-muted-foreground shadow-soft ring-1 ring-border/55">
          No live insights yet. Import a few transactions and PennyRush will have enough signal to generate useful nudges.
        </div>
      ) : (
        <div className="flex snap-x gap-4 overflow-x-auto pb-2">
          {insights.map((insight, index) => {
            const Icon =
              insight.severity === "warning" || insight.severity === "critical"
                ? AlertTriangle
                : insight.severity === "success"
                  ? CheckCircle2
                  : Info;
            return (
              <motion.article
                key={insight.id}
                animate={{ opacity: 1, y: 0 }}
                className="min-w-[260px] snap-start rounded-card bg-card p-5 shadow-soft ring-1 ring-border/55"
                initial={{ opacity: 0, y: 8 }}
                transition={{ delay: index * 0.06, duration: 0.3, ease: "easeInOut" }}
              >
                <div
                  className={cn(
                    "flex h-10 w-10 items-center justify-center rounded-2xl",
                    (insight.severity === "warning" || insight.severity === "critical") && "bg-primary/20 text-primary",
                    insight.severity === "success" && "bg-success/15 text-success",
                    insight.severity === "info" && "bg-muted text-muted-foreground",
                  )}
                >
                  <Icon className="h-5 w-5" aria-hidden="true" />
                </div>
                <h3 className="mt-4 text-base font-bold">{insight.title}</h3>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">{insight.body}</p>
              </motion.article>
            );
          })}
        </div>
      )}
    </section>
  );
}
