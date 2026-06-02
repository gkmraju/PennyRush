"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Plus, Search, Upload } from "lucide-react";
import { AppSidebar } from "@/components/features/app-sidebar";
import { ImportCard } from "@/components/features/import-card";
import { InsightsCarousel } from "@/components/features/insights-carousel";
import { ManualTransactionModal } from "@/components/features/manual-transaction-modal";
import { MetricCard } from "@/components/features/metric-card";
import { MobileNav } from "@/components/features/mobile-nav";
import { RecentTransactions } from "@/components/features/recent-transactions";
import { SpendingDonut } from "@/components/features/spending-donut";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { loadDashboardData, type DashboardData, type ProfileSettings } from "@/lib/dashboard-data";
import { createClient } from "@/lib/supabase/client";
import { buildMetrics, buildRecentTransactions, buildSpending } from "@/lib/transactions";
import { formatCurrency, formatDate } from "@/lib/utils";

const fallbackProfile: ProfileSettings = {
  currency: "USD",
  locale: "en-US",
  localOnlyMode: false,
};

export function HomeDashboard() {
  const supabase = useMemo(() => createClient(), []);
  const [dashboard, setDashboard] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [importRequest, setImportRequest] = useState(0);
  const [manualOpen, setManualOpen] = useState(false);

  const refreshDashboard = useCallback(
    async (showLoading = false) => {
      if (showLoading) setLoading(true);
      try {
        const data = await loadDashboardData(supabase);
        setDashboard(data);
        setError(null);
      } catch (loadError) {
        setError(loadError instanceof Error ? loadError.message : "Could not refresh dashboard data.");
      }
      setLoading(false);
    },
    [supabase],
  );

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const data = await loadDashboardData(supabase);
        if (!active) return;
        setDashboard(data);
        setError(null);
      } catch (loadError) {
        if (!active) return;
        setError(loadError instanceof Error ? loadError.message : "Could not load dashboard data.");
      }
      if (!active) return;
      setLoading(false);
    }

    void load();

    const channel = supabase
      .channel("web-dashboard-transactions")
      .on("postgres_changes", { event: "*", schema: "public", table: "transactions" }, () => {
        void refreshDashboard(false);
      })
      .subscribe();

    return () => {
      active = false;
      void supabase.removeChannel(channel);
    };
  }, [refreshDashboard, supabase]);

  const rows = dashboard?.transactions ?? [];
  const profile = dashboard?.profile ?? fallbackProfile;
  const categories = dashboard?.categories ?? [];
  const metrics = useMemo(() => buildMetrics(rows), [rows]);
  const recentTransactions = useMemo(() => buildRecentTransactions(rows), [rows]);
  const spending = useMemo(() => buildSpending(rows), [rows]);
  const savingsRate = metrics.income > 0 ? Math.round((metrics.saved / metrics.income) * 100) : 0;

  function requestImport() {
    setImportRequest((value) => value + 1);
    requestAnimationFrame(() => document.getElementById("statement-import")?.scrollIntoView({ behavior: "smooth", block: "center" }));
  }

  function afterMutation() {
    void refreshDashboard(false);
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="flex">
        <AppSidebar />
        <main className="min-w-0 flex-1 px-4 pb-28 pt-5 sm:px-6 lg:px-10 lg:pb-12">
          <div className="mx-auto max-w-7xl">
            <header className="flex items-center justify-between gap-4">
              <div className="min-w-0">
                <p className="text-sm text-muted-foreground">Good afternoon · {formatDate(new Date(), profile.locale)}</p>
                <h1 className="mt-2 truncate text-2xl font-bold sm:text-3xl">Your money, live</h1>
              </div>
              <div className="hidden items-center gap-2 sm:flex">
                <Button aria-label="Search" size="icon" type="button" variant="secondary">
                  <Search className="h-4 w-4" aria-hidden="true" />
                </Button>
                <Button onClick={requestImport} type="button" variant="secondary">
                  <Upload className="h-4 w-4" aria-hidden="true" />
                  Import
                </Button>
                <Button onClick={() => setManualOpen(true)} type="button">
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
                  {formatCurrency(metrics.netWorth, profile.currency, profile.locale)}
                </motion.p>
                <p className="mt-4 max-w-xl text-base leading-7 text-muted-foreground">
                  {loading ? "Syncing your Supabase transactions..." : error ?? `${rows.length} transaction${rows.length === 1 ? "" : "s"} synced from Supabase.`}
                </p>
              </div>

              <div className="grid grid-cols-3 gap-3 lg:grid-cols-1">
                <HeroMini currency={profile.currency} label="Income" locale={profile.locale} value={metrics.income} tone="text-success" />
                <HeroMini currency={profile.currency} label="Expenses" locale={profile.locale} value={metrics.expenses} tone="text-danger" />
                <HeroMini currency={profile.currency} label="Saved" locale={profile.locale} value={metrics.saved} tone="text-foreground" />
              </div>
            </section>

            <section className="mt-8 grid gap-4 sm:grid-cols-3">
              <MetricCard currency={profile.currency} delta="this month" label="Income" locale={profile.locale} tone="income" value={metrics.income} />
              <MetricCard currency={profile.currency} delta="this month" label="Expenses" locale={profile.locale} tone="expense" value={metrics.expenses} />
              <MetricCard currency={profile.currency} delta={`${savingsRate}% savings rate`} label="Saved" locale={profile.locale} tone="neutral" value={metrics.saved} />
            </section>

            <section className="mt-8 grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
              <SpendingDonut currency={profile.currency} locale={profile.locale} spending={spending} />
              <ImportCard
                categories={categories}
                existingTransactions={rows}
                onImported={afterMutation}
                openRequest={importRequest}
                profile={profile}
                supabase={supabase}
                userId={dashboard?.userId ?? null}
              />
            </section>

            <section className="mt-8 grid gap-8 xl:grid-cols-[1fr_420px]">
              <InsightsCarousel insights={dashboard?.insights ?? []} />
              <RecentTransactions currency={profile.currency} locale={profile.locale} transactions={recentTransactions} />
            </section>
          </div>
        </main>
      </div>

      <MobileNav onAdd={() => setManualOpen(true)} />
      <ManualTransactionModal
        categories={categories}
        onClose={() => setManualOpen(false)}
        onSaved={afterMutation}
        open={manualOpen}
        profile={profile}
        supabase={supabase}
        userId={dashboard?.userId ?? null}
      />
    </div>
  );
}

function HeroMini({
  label,
  value,
  tone,
  currency,
  locale,
}: {
  label: string;
  value: number;
  tone: string;
  currency: string;
  locale: string;
}) {
  return (
    <div className="rounded-card bg-muted px-4 py-3">
      <p className="text-sm font-semibold text-muted-foreground">{label}</p>
      <p className={`tabular mt-2 text-base font-bold ${tone}`}>{formatCurrency(value, currency, locale)}</p>
    </div>
  );
}
