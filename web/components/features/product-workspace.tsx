"use client";

import { useMemo, useState, type FormEvent, type InputHTMLAttributes } from "react";
import { Plus, Save, Search, Trash2 } from "lucide-react";
import {
  deleteBudget,
  deleteCategory,
  deleteGoal,
  deleteTransaction,
  saveBudget,
  saveCategory,
  saveGoal,
  updateManualTransaction,
  updateProfileSettings,
  type BudgetRow,
  type CategoryRow,
  type GoalRow,
  type ProfileSettings,
} from "@/lib/dashboard-data";
import type { TransactionType } from "@pennyrush/shared";
import type { TransactionRow } from "@/lib/transactions";
import { formatCurrency } from "@/lib/utils";
import type { SupabaseClient } from "@supabase/supabase-js";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

const colorOptions = ["#FDE68A", "#BFDBFE", "#BBF7D0", "#FBCFE8", "#DDD6FE", "#FED7AA", "#A7F3D0", "#C7D2FE"];

export function ActivityWorkspace({
  categories,
  currency,
  locale,
  onChanged,
  rows,
  supabase,
  userId,
}: {
  categories: CategoryRow[];
  currency: string;
  locale: string;
  onChanged: () => void;
  rows: TransactionRow[];
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<"all" | "income" | "expense">("all");
  const [editing, setEditing] = useState<TransactionRow | null>(null);
  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return rows.filter((row) => {
      const text = `${row.merchant ?? ""} ${row.note ?? ""} ${row.category?.name ?? ""}`.toLowerCase();
      const typeMatch = filter === "all" || row.type === filter;
      return typeMatch && (!q || text.includes(q));
    });
  }, [filter, query, rows]);

  return (
    <Card id="activity-workspace">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-muted-foreground">Activity</p>
          <h2 className="mt-1 text-xl font-bold">Search, correct, and clean up entries</h2>
        </div>
        <div className="rounded-card bg-muted px-3 py-2 text-sm font-semibold text-muted-foreground">
          {filtered.length} shown
        </div>
      </div>

      <div className="mt-5 grid gap-3 lg:grid-cols-[1fr_280px]">
        <label className="flex min-h-11 items-center gap-3 rounded-chip bg-muted px-4 text-sm font-semibold text-muted-foreground">
          <Search className="h-4 w-4" aria-hidden="true" />
          <input
            className="min-w-0 flex-1 bg-transparent text-foreground outline-none placeholder:text-muted-foreground"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search merchant, category, note"
            value={query}
          />
        </label>
        <div className="grid grid-cols-3 gap-2">
          {(["all", "income", "expense"] as const).map((item) => (
            <button
              className="rounded-chip bg-muted px-3 py-2 text-sm font-bold text-muted-foreground data-[active=true]:bg-primary data-[active=true]:text-primary-foreground"
              data-active={filter === item}
              key={item}
              onClick={() => setFilter(item)}
              type="button"
            >
              {item === "all" ? "All" : item === "income" ? "Income" : "Spend"}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-5 divide-y divide-border/65 overflow-hidden rounded-card border border-border/65">
        {filtered.length === 0 ? (
          <p className="bg-muted/45 p-5 text-sm text-muted-foreground">No entries match this view.</p>
        ) : (
          filtered.slice(0, 80).map((row) => {
            const signed = row.type === "expense" ? -Number(row.amount) : Number(row.amount);
            return (
              <button
                className="grid w-full grid-cols-[1fr_auto] items-center gap-3 bg-card px-4 py-3 text-left transition hover:bg-muted/55"
                key={row.id}
                onClick={() => setEditing(row)}
                type="button"
              >
                <span className="min-w-0">
                  <span className="block truncate text-sm font-bold">{row.merchant || row.note || "Entry"}</span>
                  <span className="mt-1 block truncate text-xs font-semibold text-muted-foreground">
                    {row.category?.name ?? "Uncategorized"} · {row.date} · {row.source === "ocr" ? "Receipt" : row.source}
                  </span>
                </span>
                <span className={`tabular text-sm font-bold ${signed >= 0 ? "text-success" : "text-foreground"}`}>
                  {signed >= 0 ? "+" : ""}
                  {formatCurrency(signed, currency, locale)}
                </span>
              </button>
            );
          })
        )}
      </div>

      {editing ? (
        <TransactionEditor
          categories={categories}
          currency={currency}
          locale={locale}
          onChanged={onChanged}
          onClose={() => setEditing(null)}
          row={editing}
          supabase={supabase}
          userId={userId}
        />
      ) : null}
    </Card>
  );
}

function TransactionEditor({
  categories,
  onChanged,
  onClose,
  row,
  supabase,
  userId,
}: {
  categories: CategoryRow[];
  currency: string;
  locale: string;
  onChanged: () => void;
  onClose: () => void;
  row: TransactionRow;
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const [merchant, setMerchant] = useState(row.merchant ?? "");
  const [amount, setAmount] = useState(String(row.amount));
  const [date, setDate] = useState(row.date);
  const [type, setType] = useState<TransactionType>(row.type === "income" ? "income" : "expense");
  const [categoryId, setCategoryId] = useState(row.category_id ?? "");
  const [note, setNote] = useState(row.note ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId) return;
    setSaving(true);
    setError(null);
    try {
      await updateManualTransaction(supabase, userId, categories, {
        id: row.id,
        amount: Number(amount),
        type,
        date,
        merchant,
        note,
        category_id: categoryId || null,
      });
      onChanged();
      onClose();
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save this entry.");
    }
    setSaving(false);
  }

  async function remove() {
    if (!userId || !window.confirm("Delete this activity entry?")) return;
    setSaving(true);
    try {
      await deleteTransaction(supabase, userId, row.id);
      onChanged();
      onClose();
    } catch (deleteError) {
      setError(deleteError instanceof Error ? deleteError.message : "Could not delete this entry.");
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-end bg-neutral-950/45 p-0 sm:place-items-center sm:p-6">
      <form className="w-full max-w-xl rounded-t-card bg-card p-5 shadow-soft ring-1 ring-border/65 sm:rounded-card" onSubmit={submit}>
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-sm font-semibold text-muted-foreground">Edit entry</p>
            <h3 className="mt-1 text-xl font-bold">Correct activity details</h3>
          </div>
          <Button onClick={onClose} type="button" variant="ghost">Close</Button>
        </div>
        <div className="mt-5 grid gap-3 sm:grid-cols-2">
          <Field label="Merchant" value={merchant} onChange={setMerchant} />
          <Field label="Amount" type="number" min="0" step="0.01" value={amount} onChange={setAmount} />
          <Field label="Date" type="date" value={date} onChange={setDate} />
          <label className="text-sm font-semibold">
            Type
            <select className="mt-2 h-11 w-full rounded-chip bg-muted px-3 outline-none" onChange={(event) => setType(event.target.value as "income" | "expense")} value={type}>
              <option value="expense">Spend</option>
              <option value="income">Income</option>
            </select>
          </label>
          <label className="text-sm font-semibold sm:col-span-2">
            Category
            <select className="mt-2 h-11 w-full rounded-chip bg-muted px-3 outline-none" onChange={(event) => setCategoryId(event.target.value)} value={categoryId}>
              <option value="">Uncategorized</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>{category.name}</option>
              ))}
            </select>
          </label>
          <Field className="sm:col-span-2" label="Note" value={note} onChange={setNote} />
        </div>
        {error ? <p className="mt-4 rounded-card bg-danger/10 px-4 py-3 text-sm font-semibold text-danger">{error}</p> : null}
        <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-between">
          <Button disabled={saving} onClick={remove} type="button" variant="danger">
            <Trash2 className="h-4 w-4" aria-hidden="true" />
            Delete
          </Button>
          <Button disabled={saving || !merchant.trim() || !Number.isFinite(Number(amount))} type="submit">
            <Save className="h-4 w-4" aria-hidden="true" />
            {saving ? "Saving..." : "Save entry"}
          </Button>
        </div>
      </form>
    </div>
  );
}

export function PlanWorkspace({
  budgets,
  categories,
  currency,
  goals,
  locale,
  onChanged,
  rows,
  supabase,
  userId,
}: {
  budgets: BudgetRow[];
  categories: CategoryRow[];
  currency: string;
  goals: GoalRow[];
  locale: string;
  onChanged: () => void;
  rows: TransactionRow[];
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const spentByCategory = useMemo(() => {
    const map = new Map<string, number>();
    const now = new Date();
    for (const row of rows) {
      const date = new Date(`${row.date}T00:00:00`);
      if (row.type !== "expense" || date.getFullYear() !== now.getFullYear() || date.getMonth() !== now.getMonth()) continue;
      const key = row.category_id ?? "other";
      map.set(key, (map.get(key) ?? 0) + Number(row.amount));
    }
    return map;
  }, [rows]);

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <BudgetEditor budgets={budgets} categories={categories} currency={currency} locale={locale} onChanged={onChanged} spentByCategory={spentByCategory} supabase={supabase} userId={userId} />
      <GoalEditor goals={goals} currency={currency} locale={locale} onChanged={onChanged} supabase={supabase} userId={userId} />
    </div>
  );
}

function BudgetEditor({
  budgets,
  categories,
  currency,
  locale,
  onChanged,
  spentByCategory,
  supabase,
  userId,
}: {
  budgets: BudgetRow[];
  categories: CategoryRow[];
  currency: string;
  locale: string;
  onChanged: () => void;
  spentByCategory: Map<string, number>;
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const [categoryId, setCategoryId] = useState(categories[0]?.id ?? "");
  const [amount, setAmount] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId || !categoryId) return;
    setSaving(true);
    const existing = budgets.find((budget) => budget.category_id === categoryId);
    await saveBudget(supabase, userId, { id: existing?.id, category_id: categoryId, amount: Number(amount) });
    setAmount("");
    onChanged();
    setSaving(false);
  }

  return (
    <Card>
      <h2 className="text-xl font-bold">Monthly budgets</h2>
      <form className="mt-5 grid gap-3 sm:grid-cols-[1fr_160px_auto]" onSubmit={submit}>
        <select className="h-11 rounded-chip bg-muted px-3 text-sm font-semibold outline-none" onChange={(event) => setCategoryId(event.target.value)} value={categoryId}>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>{category.name}</option>
          ))}
        </select>
        <input className="h-11 rounded-chip bg-muted px-3 text-sm font-semibold outline-none" min="0" onChange={(event) => setAmount(event.target.value)} placeholder="Limit" step="0.01" type="number" value={amount} />
        <Button disabled={saving || !categoryId || Number(amount) <= 0} type="submit">
          <Plus className="h-4 w-4" aria-hidden="true" />
          Save
        </Button>
      </form>
      <div className="mt-5 space-y-3">
        {budgets.length === 0 ? <p className="rounded-card bg-muted p-4 text-sm text-muted-foreground">Add category limits to turn activity into a plan.</p> : null}
        {budgets.map((budget) => {
          const spent = spentByCategory.get(budget.category_id) ?? 0;
          const progress = budget.amount > 0 ? Math.min(100, Math.round((spent / budget.amount) * 100)) : 0;
          return (
            <div className="rounded-card bg-muted p-4" key={budget.id}>
              <div className="flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-bold">{budget.category?.name ?? "Category"}</p>
                  <p className="mt-1 text-xs font-semibold text-muted-foreground">
                    {formatCurrency(spent, currency, locale)} of {formatCurrency(budget.amount, currency, locale)}
                  </p>
                </div>
                <Button aria-label={`Remove ${budget.category?.name ?? "budget"}`} onClick={() => userId && deleteBudget(supabase, userId, budget.id).then(onChanged)} size="icon" type="button" variant="ghost">
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </Button>
              </div>
              <div className="mt-3 h-2 overflow-hidden rounded-full bg-background">
                <div className="h-full rounded-full bg-primary" style={{ width: `${Math.max(4, progress)}%` }} />
              </div>
            </div>
          );
        })}
      </div>
    </Card>
  );
}

function GoalEditor({
  goals,
  currency,
  locale,
  onChanged,
  supabase,
  userId,
}: {
  goals: GoalRow[];
  currency: string;
  locale: string;
  onChanged: () => void;
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const [name, setName] = useState("");
  const [target, setTarget] = useState("");
  const [current, setCurrent] = useState("");
  const [date, setDate] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId) return;
    await saveGoal(supabase, userId, {
      name,
      target_amount: Number(target),
      current_amount: Number(current || 0),
      target_date: date || null,
    });
    setName("");
    setTarget("");
    setCurrent("");
    setDate("");
    onChanged();
  }

  return (
    <Card>
      <h2 className="text-xl font-bold">Goals</h2>
      <form className="mt-5 grid gap-3 sm:grid-cols-2" onSubmit={submit}>
        <Field label="Goal" value={name} onChange={setName} />
        <Field label="Target" min="0" step="0.01" type="number" value={target} onChange={setTarget} />
        <Field label="Saved" min="0" step="0.01" type="number" value={current} onChange={setCurrent} />
        <Field label="Target date" type="date" value={date} onChange={setDate} />
        <Button className="sm:col-span-2" disabled={!userId || !name.trim() || Number(target) <= 0} type="submit">
          <Plus className="h-4 w-4" aria-hidden="true" />
          Add goal
        </Button>
      </form>
      <div className="mt-5 space-y-3">
        {goals.length === 0 ? <p className="rounded-card bg-muted p-4 text-sm text-muted-foreground">Create goals for emergency funds, trips, or big purchases.</p> : null}
        {goals.map((goal) => {
          const progress = goal.target_amount > 0 ? Math.min(100, Math.round((goal.current_amount / goal.target_amount) * 100)) : 0;
          return (
            <div className="rounded-card bg-muted p-4" key={goal.id}>
              <div className="flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-bold">{goal.name}</p>
                  <p className="mt-1 text-xs font-semibold text-muted-foreground">
                    {formatCurrency(goal.current_amount, currency, locale)} of {formatCurrency(goal.target_amount, currency, locale)}
                    {goal.target_date ? ` · ${goal.target_date}` : ""}
                  </p>
                </div>
                <Button aria-label={`Remove ${goal.name}`} onClick={() => userId && deleteGoal(supabase, userId, goal.id).then(onChanged)} size="icon" type="button" variant="ghost">
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </Button>
              </div>
              <div className="mt-3 h-2 overflow-hidden rounded-full bg-background">
                <div className="h-full rounded-full bg-success" style={{ width: `${Math.max(4, progress)}%` }} />
              </div>
            </div>
          );
        })}
      </div>
    </Card>
  );
}

export function AccountWorkspace({
  categories,
  onChanged,
  profile,
  supabase,
  userId,
}: {
  categories: CategoryRow[];
  onChanged: () => void;
  profile: ProfileSettings;
  supabase: SupabaseClient;
  userId: string | null;
}) {
  const [currency, setCurrency] = useState(profile.currency);
  const [locale, setLocale] = useState(profile.locale);
  const [name, setName] = useState("");
  const [color, setColor] = useState(colorOptions[0]);

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId) return;
    await updateProfileSettings(supabase, userId, { currency, locale });
    onChanged();
  }

  async function addCategory(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId || !name.trim()) return;
    await saveCategory(supabase, userId, { name, color });
    setName("");
    onChanged();
  }

  return (
    <div className="grid gap-6 xl:grid-cols-[0.85fr_1.15fr]">
      <Card>
        <h2 className="text-xl font-bold">Money settings</h2>
        <form className="mt-5 grid gap-3" onSubmit={saveProfile}>
          <Field label="Currency code" maxLength={3} value={currency} onChange={setCurrency} />
          <Field label="Locale" value={locale} onChange={setLocale} />
          <Button disabled={!userId || currency.trim().length !== 3} type="submit">
            <Save className="h-4 w-4" aria-hidden="true" />
            Save settings
          </Button>
        </form>
      </Card>
      <Card>
        <h2 className="text-xl font-bold">Categories</h2>
        <form className="mt-5 grid gap-3 sm:grid-cols-[1fr_auto_auto]" onSubmit={addCategory}>
          <Field label="New category" value={name} onChange={setName} />
          <label className="text-sm font-semibold">
            Color
            <select className="mt-2 h-11 rounded-chip bg-muted px-3 outline-none" onChange={(event) => setColor(event.target.value)} value={color}>
              {colorOptions.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </label>
          <Button className="self-end" disabled={!userId || !name.trim()} type="submit">
            <Plus className="h-4 w-4" aria-hidden="true" />
            Add
          </Button>
        </form>
        <div className="mt-5 grid gap-2 sm:grid-cols-2">
          {categories.map((category) => (
            <div className="flex items-center justify-between gap-3 rounded-card bg-muted px-3 py-2" key={category.id}>
              <span className="flex min-w-0 items-center gap-2">
                <span className="h-3 w-3 shrink-0 rounded-full" style={{ backgroundColor: category.color }} />
                <span className="truncate text-sm font-bold">{category.name}</span>
              </span>
              {category.is_system ? (
                <span className="text-xs font-semibold text-muted-foreground">Default</span>
              ) : (
                <Button aria-label={`Delete ${category.name}`} onClick={() => userId && deleteCategory(supabase, userId, category.id).then(onChanged)} size="icon" type="button" variant="ghost">
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </Button>
              )}
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}

export function LocalInsightWorkspace({
  currency,
  locale,
  rows,
}: {
  currency: string;
  locale: string;
  rows: TransactionRow[];
}) {
  const insights = useMemo(() => buildLocalInsights(rows, currency, locale), [currency, locale, rows]);
  return (
    <Card>
      <h2 className="text-xl font-bold">Money signals</h2>
      <div className="mt-5 grid gap-3 lg:grid-cols-3">
        {insights.map((insight) => (
          <div className="rounded-card bg-muted p-4" key={insight.title}>
            <p className="text-sm font-bold">{insight.title}</p>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">{insight.body}</p>
          </div>
        ))}
      </div>
    </Card>
  );
}

function buildLocalInsights(rows: TransactionRow[], currency: string, locale: string) {
  if (rows.length === 0) {
    return [
      { title: "Start with one entry", body: "Add a receipt, a manual entry, or a statement to unlock useful money signals." },
      { title: "Plan after activity", body: "Budgets and goals become more helpful once spending categories are visible." },
      { title: "Keep it clean", body: "Correct categories after imports so future insights become sharper." },
    ];
  }
  const now = new Date();
  const monthRows = rows.filter((row) => {
    const date = new Date(`${row.date}T00:00:00`);
    return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
  });
  const income = monthRows.filter((row) => row.type === "income").reduce((sum, row) => sum + Number(row.amount), 0);
  const spend = monthRows.filter((row) => row.type === "expense").reduce((sum, row) => sum + Number(row.amount), 0);
  const topMerchant = [...monthRows.filter((row) => row.type === "expense").reduce((map, row) => {
    const key = row.merchant || row.note || "Unknown";
    map.set(key, (map.get(key) ?? 0) + Number(row.amount));
    return map;
  }, new Map<string, number>())].sort((a, b) => b[1] - a[1])[0];
  const savingsRate = income > 0 ? Math.round(((income - spend) / income) * 100) : null;
  return [
    {
      title: "This month",
      body: `${formatCurrency(spend, currency, locale)} spent${income > 0 ? ` against ${formatCurrency(income, currency, locale)} income` : ""}.`,
    },
    {
      title: "Savings rate",
      body: savingsRate === null ? "Add income entries to see a savings rate." : `${savingsRate}% of income is left after tracked spend.`,
    },
    {
      title: "Merchant watch",
      body: topMerchant ? `${topMerchant[0]} is your highest tracked merchant at ${formatCurrency(topMerchant[1], currency, locale)}.` : "No merchant pattern yet this month.",
    },
  ];
}

function Field({
  className,
  label,
  onChange,
  value,
  ...props
}: {
  className?: string;
  label: string;
  onChange: (value: string) => void;
  value: string;
} & Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value">) {
  return (
    <label className={`text-sm font-semibold ${className ?? ""}`}>
      {label}
      <input
        className="mt-2 h-11 w-full rounded-chip bg-muted px-3 text-sm font-semibold outline-none"
        onChange={(event) => onChange(event.target.value)}
        value={value}
        {...props}
      />
    </label>
  );
}
