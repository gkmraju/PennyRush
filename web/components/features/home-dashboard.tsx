"use client";

import { motion } from "framer-motion";
import { Plus, Search, Upload } from "lucide-react";
import { AppSidebar } from "@/components/features/app-sidebar";
import { ImportCard } from "@/components/features/import-card";
import { InsightsCarousel } from "@/components/features/insights-carousel";
import { MetricCard } from "@/components/features/metric-card";
import { MobileNav } from "@/components/features/mobile-nav";
import { RecentTransactions } from "@/components/features/recent-transactions";
import { SpendingDonut } from "@/components/features/spending-donut";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { metrics } from "@/lib/sample-data";
import { formatCurrency, formatDate } from "@/lib/utils";

export function HomeDashboard() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="flex">
        <AppSidebar />
        <main className="min-w-0 flex-1 px-4 pb-28 pt-5 sm:px-6 lg:px-10 lg:pb-12">
          <div className="mx-auto max-w-7xl">
            <header className="flex items-center justify-between gap-4">
              <div className="min-w-0">
                <p className="text-sm text-muted-foreground">Good afternoon · {formatDate(new Date())}</p>
                <h1 className="mt-2 truncate text-2xl font-bold sm:text-3xl">Your money, live</h1>
              </div>
              <div className="hidden items-center gap-2 sm:flex">
                <Button aria-label="Search" size="icon" type="button" variant="secondary">
                  <Search className="h-4 w-4" aria-hidden="true" />
                </Button>
                <Button type="button" variant="secondary">
                  <Upload className="h-4 w-4" aria-hidden="true" />
                  Import
                </Button>
                <Button type="button">
                  <Plus className="h-4 w-4" aria-hidden="true" />
                  Add
                </Button>
              </div>
            </header>

            <section className="mt-10 grid gap-6 lg:grid-cols-[1fr_360px]">
              <div>
                <Badge>Net worth</Badge>
                <motion.p
                  animate={{ opacity: 1, y: 0 }}
                  className="tabular mt-5 text-5xl font-bold tracking-normal sm:text-6xl"
                  initial={{ opacity: 0, y: 12 }}
                  transition={{ duration: 0.3, ease: "easeInOut" }}
                >
                  {formatCurrency(metrics.netWorth)}
                </motion.p>
                <p className="mt-4 max-w-xl text-base leading-7 text-muted-foreground">
                  Up {formatCurrency(1240)} this month after bills, goals, and subscriptions.
                </p>
              </div>

              <div className="grid grid-cols-3 gap-3 lg:grid-cols-1">
                <HeroMini label="Income" value={metrics.income} tone="text-success" />
                <HeroMini label="Expenses" value={metrics.expenses} tone="text-danger" />
                <HeroMini label="Saved" value={metrics.saved} tone="text-foreground" />
              </div>
            </section>

            <section className="mt-8 grid gap-4 sm:grid-cols-3">
              <MetricCard delta="+12% vs last month" label="Income" tone="income" value={metrics.income} />
              <MetricCard delta="-4% vs last month" label="Expenses" tone="expense" value={metrics.expenses} />
              <MetricCard delta="29% savings rate" label="Saved" tone="neutral" value={metrics.saved} />
            </section>

            <section className="mt-8 grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
              <SpendingDonut />
              <ImportCard />
            </section>

            <section className="mt-8 grid gap-8 xl:grid-cols-[1fr_420px]">
              <InsightsCarousel />
              <RecentTransactions />
            </section>
          </div>
        </main>
      </div>

      <MobileNav onAdd={() => undefined} />
    </div>
  );
}

function HeroMini({ label, value, tone }: { label: string; value: number; tone: string }) {
  return (
    <div className="rounded-card bg-muted px-4 py-3">
      <p className="text-xs font-semibold text-muted-foreground">{label}</p>
      <p className={`tabular mt-2 text-sm font-bold sm:text-base ${tone}`}>{formatCurrency(value)}</p>
    </div>
  );
}
