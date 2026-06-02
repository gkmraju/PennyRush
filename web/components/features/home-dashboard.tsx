"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { BarChart3, Camera, Download, Plus, ReceiptText, Search, Upload, type LucideIcon } from "lucide-react";
import { AppSidebar } from "@/components/features/app-sidebar";
import { ImportCard } from "@/components/features/import-card";
import { InsightsCarousel } from "@/components/features/insights-carousel";
import { ManualTransactionModal } from "@/components/features/manual-transaction-modal";
import { MetricCard } from "@/components/features/metric-card";
import { MobileNav } from "@/components/features/mobile-nav";
import { RecentTransactions } from "@/components/features/recent-transactions";
import { SpendingDonut } from "@/components/features/spending-donut";
import { Button } from "@/components/ui/button";
import { loadDashboardData, type DashboardData, type ProfileSettings } from "@/lib/dashboard-data";
import { createClient } from "@/lib/supabase/client";
import { buildMetrics, buildRecentTransactions, buildSpending, type DashboardTransaction } from "@/lib/transactions";
import { formatCurrency } from "@/lib/utils";

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
  const recentMerchants = useMemo(() => buildRecentMerchants(recentTransactions), [recentTransactions]);

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
          <div className="mx-auto max-w-6xl">
            <header className="flex items-center gap-3">
              <button
                className="flex h-12 min-w-0 flex-1 items-center gap-3 rounded-full bg-muted px-4 text-left text-sm font-semibold text-muted-foreground ring-1 ring-border/55 transition hover:bg-muted/75"
                type="button"
              >
                <Search className="h-4 w-4 shrink-0" aria-hidden="true" />
                <span className="truncate">Search activity, merchants, notes</span>
              </button>
              <Button onClick={requestImport} size="icon" type="button" variant="secondary" aria-label="Import statement">
                <Upload className="h-4 w-4" aria-hidden="true" />
              </Button>
              <Button onClick={() => setManualOpen(true)} size="icon" type="button" aria-label="Add entry">
                <Plus className="h-4 w-4" aria-hidden="true" />
              </Button>
            </header>

            <section className="mt-6 grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
              <motion.div
                animate={{ opacity: 1, y: 0 }}
                className="rounded-card bg-card p-6 shadow-soft ring-1 ring-border/55"
                initial={{ opacity: 0, y: 12 }}
                transition={{ duration: 0.28, ease: "easeInOut" }}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0">
                    <p className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">PennyRush wallet</p>
                    <h1 className="tabular mt-4 text-4xl font-bold tracking-normal sm:text-5xl">
                      {formatCurrency(metrics.netWorth, profile.currency, profile.locale)}
                    </h1>
                  </div>
                  <span className="rounded-full bg-primary/15 px-3 py-1 text-sm font-bold text-foreground">{profile.currency}</span>
                </div>
                <p className="mt-4 max-w-2xl text-sm leading-6 text-muted-foreground">
                  {loading ? "Updating your activity..." : error ?? `${rows.length} ${rows.length === 1 ? "entry" : "entries"} ready.`}
                </p>
                <div className="mt-5 h-2 overflow-hidden rounded-full bg-muted">
                  <div className="h-full rounded-full bg-success" style={{ width: `${Math.max(6, Math.min(100, savingsRate))}%` }} />
                </div>
                <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
                  <HeroMini currency={profile.currency} label="Income" locale={profile.locale} value={metrics.income} tone="text-success" />
                  <HeroMini currency={profile.currency} label="Spend" locale={profile.locale} value={metrics.expenses} tone="text-danger" />
                  <HeroMini currency={profile.currency} label="Saved" locale={profile.locale} value={metrics.saved} tone="text-foreground" />
                </div>
              </motion.div>

              <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-2">
                <QuickAction label="Add" icon={Plus} onClick={() => setManualOpen(true)} />
                <QuickAction label="Import" icon={Upload} onClick={requestImport} />
                <QuickAction label="Review" icon={ReceiptText} onClick={() => document.getElementById("latest-activity")?.scrollIntoView({ behavior: "smooth", block: "start" })} />
                <QuickAction label="Plan" icon={BarChart3} onClick={() => document.getElementById("money-plan")?.scrollIntoView({ behavior: "smooth", block: "start" })} />
              </div>
            </section>

            <section className="mt-8">
              <div className="mb-4 flex items-center justify-between gap-4">
                <h2 className="text-xl font-bold">Recent</h2>
                <button className="text-sm font-semibold text-muted-foreground" type="button">
                  View all
                </button>
              </div>
              <div className="-mx-4 flex gap-4 overflow-x-auto px-4 pb-2 sm:mx-0 sm:px-0">
                {recentMerchants.length > 0 ? (
                  recentMerchants.map((merchant) => <RecentMerchant key={merchant.id} merchant={merchant} />)
                ) : (
                  <div className="rounded-card bg-muted px-4 py-3 text-sm text-muted-foreground">Add or import activity to build your recent list.</div>
                )}
              </div>
            </section>

            <section className="mt-8" id="latest-activity">
              <RecentTransactions currency={profile.currency} locale={profile.locale} transactions={recentTransactions.slice(0, 5)} />
            </section>

            <section className="mt-8 grid gap-4 sm:grid-cols-3" id="money-plan">
              <MetricCard currency={profile.currency} delta="this month" label="Income" locale={profile.locale} tone="income" value={metrics.income} />
              <MetricCard currency={profile.currency} delta="this month" label="Spend" locale={profile.locale} tone="expense" value={metrics.expenses} />
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
              <SecondaryActionCard onImport={requestImport} onScan={() => setManualOpen(true)} />
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

function QuickAction({
  label,
  icon: Icon,
  onClick,
}: {
  label: string;
  icon: LucideIcon;
  onClick: () => void;
}) {
  return (
    <button
      className="flex min-h-24 flex-col items-center justify-center gap-3 rounded-card bg-card px-3 text-sm font-bold shadow-soft ring-1 ring-border/55 transition hover:bg-muted/55"
      onClick={onClick}
      type="button"
    >
      <span className="flex h-11 w-11 items-center justify-center rounded-full bg-primary/15 text-foreground">
        <Icon className="h-5 w-5" aria-hidden="true" />
      </span>
      {label}
    </button>
  );
}

type RecentMerchantItem = {
  id: string;
  name: string;
  category: string;
  color: string;
};

function buildRecentMerchants(transactions: DashboardTransaction[]): RecentMerchantItem[] {
  const seen = new Set<string>();
  const merchants: RecentMerchantItem[] = [];
  for (const transaction of transactions) {
    const name = transaction.merchant.trim();
    const key = name.toLowerCase();
    if (!name || seen.has(key)) continue;
    seen.add(key);
    merchants.push({
      id: transaction.id,
      name,
      category: transaction.category,
      color: transaction.color,
    });
    if (merchants.length >= 10) break;
  }
  return merchants;
}

function RecentMerchant({ merchant }: { merchant: RecentMerchantItem }) {
  return (
    <button className="w-20 shrink-0 text-center" type="button" aria-label={`View activity for ${merchant.name}`}>
      <span
        className="mx-auto flex h-14 w-14 items-center justify-center rounded-full text-base font-bold text-neutral-950 ring-1 ring-border/45"
        style={{ backgroundColor: merchant.color }}
      >
        {merchant.name.charAt(0).toUpperCase()}
      </span>
      <span className="mt-2 block truncate text-sm font-semibold">{merchant.name}</span>
      <span className="mt-0.5 block truncate text-xs text-muted-foreground">{merchant.category}</span>
    </button>
  );
}

function SecondaryActionCard({ onImport, onScan }: { onImport: () => void; onScan: () => void }) {
  return (
    <section className="rounded-card bg-card p-5 shadow-soft ring-1 ring-border/55">
      <h2 className="text-xl font-bold">Add activity</h2>
      <p className="mt-2 text-sm leading-6 text-muted-foreground">Bring in statements or receipts when you want a fuller money picture.</p>
      <div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
        <Button onClick={onImport} type="button" variant="secondary">
          <Download className="h-4 w-4" aria-hidden="true" />
          Import statement
        </Button>
        <Button onClick={onScan} type="button" variant="secondary">
          <Camera className="h-4 w-4" aria-hidden="true" />
          Add receipt
        </Button>
      </div>
    </section>
  );
}
