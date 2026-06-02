"use client";

import { useEffect, useMemo, useState, type FormEvent } from "react";
import type { SupabaseClient } from "@supabase/supabase-js";
import { X } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  saveManualTransaction,
  type CategoryRow,
  type ProfileSettings,
} from "@/lib/dashboard-data";
import { formatCurrency } from "@/lib/utils";

export function ManualTransactionModal({
  open,
  supabase,
  userId,
  profile,
  categories,
  onClose,
  onSaved,
}: {
  open: boolean;
  supabase: SupabaseClient;
  userId: string | null;
  profile: ProfileSettings;
  categories: CategoryRow[];
  onClose: () => void;
  onSaved: () => void;
}) {
  const [type, setType] = useState<"expense" | "income">("expense");
  const [amount, setAmount] = useState("");
  const [merchant, setMerchant] = useState("");
  const [note, setNote] = useState("");
  const [date, setDate] = useState(todayIso());
  const [categoryId, setCategoryId] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const parsedAmount = Number(amount);

  useEffect(() => {
    if (!open) return;
    setError(null);
  }, [open]);

  const previewAmount = useMemo(() => {
    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) return null;
    return type === "income" ? parsedAmount : -parsedAmount;
  }, [parsedAmount, type]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId || !Number.isFinite(parsedAmount) || parsedAmount <= 0 || !merchant.trim()) return;

    setSaving(true);
    setError(null);
    try {
      await saveManualTransaction(supabase, userId, profile, categories, {
        amount: parsedAmount,
        type,
        date,
        merchant,
        note,
        category_id: categoryId || null,
      });
      setAmount("");
      setMerchant("");
      setNote("");
      setCategoryId("");
      setDate(todayIso());
      setType("expense");
      onSaved();
      onClose();
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save this transaction.");
    } finally {
      setSaving(false);
    }
  }

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 grid place-items-end bg-black/30 px-3 py-3 sm:place-items-center">
      <form
        className="w-full max-w-md rounded-card bg-card p-5 text-card-foreground shadow-soft ring-1 ring-border"
        onSubmit={submit}
      >
        <div className="flex items-center justify-between gap-3">
          <div>
            <p className="text-sm text-muted-foreground">Manual entry</p>
            <h2 className="mt-1 text-xl font-bold">Add transaction</h2>
          </div>
          <Button aria-label="Close" onClick={onClose} size="icon" type="button" variant="secondary">
            <X className="h-4 w-4" aria-hidden="true" />
          </Button>
        </div>

        <div className="mt-5 grid grid-cols-2 gap-2 rounded-card bg-muted p-1">
          <button
            className="h-10 rounded-chip text-sm font-bold data-[active=true]:bg-card data-[active=true]:shadow-soft"
            data-active={type === "expense"}
            onClick={() => setType("expense")}
            type="button"
          >
            Expense
          </button>
          <button
            className="h-10 rounded-chip text-sm font-bold data-[active=true]:bg-card data-[active=true]:shadow-soft"
            data-active={type === "income"}
            onClick={() => setType("income")}
            type="button"
          >
            Income
          </button>
        </div>

        <label className="mt-4 block text-sm font-semibold">
          Amount
          <input
            className="mt-2 h-11 w-full rounded-chip border border-border bg-background px-3 text-foreground outline-none focus:ring-2 focus:ring-primary"
            inputMode="decimal"
            min="0"
            onChange={(event) => setAmount(event.target.value.replace(/[^0-9.]/g, ""))}
            placeholder="0.00"
            required
            step="0.01"
            type="number"
            value={amount}
          />
        </label>

        <label className="mt-4 block text-sm font-semibold">
          Merchant
          <input
            className="mt-2 h-11 w-full rounded-chip border border-border bg-background px-3 text-foreground outline-none focus:ring-2 focus:ring-primary"
            onChange={(event) => setMerchant(event.target.value)}
            placeholder="Coffee, payroll, rent..."
            required
            type="text"
            value={merchant}
          />
        </label>

        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <label className="block text-sm font-semibold">
            Date
            <input
              className="mt-2 h-11 w-full rounded-chip border border-border bg-background px-3 text-foreground outline-none focus:ring-2 focus:ring-primary"
              onChange={(event) => setDate(event.target.value)}
              required
              type="date"
              value={date}
            />
          </label>
          <label className="block text-sm font-semibold">
            Category
            <select
              className="mt-2 h-11 w-full rounded-chip border border-border bg-background px-3 text-foreground outline-none focus:ring-2 focus:ring-primary"
              onChange={(event) => setCategoryId(event.target.value)}
              value={categoryId}
            >
              <option value="">Auto</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label className="mt-4 block text-sm font-semibold">
          Note
          <input
            className="mt-2 h-11 w-full rounded-chip border border-border bg-background px-3 text-foreground outline-none focus:ring-2 focus:ring-primary"
            onChange={(event) => setNote(event.target.value)}
            placeholder="Optional"
            type="text"
            value={note}
          />
        </label>

        {previewAmount !== null ? (
          <p className="mt-4 rounded-card bg-muted px-3 py-2 text-sm text-muted-foreground">
            Preview: <span className="tabular font-bold text-foreground">{formatCurrency(previewAmount, profile.currency, profile.locale)}</span>
          </p>
        ) : null}

        {error ? <p className="mt-4 rounded-card bg-muted px-3 py-2 text-sm text-danger">{error}</p> : null}

        <Button className="mt-5 w-full" disabled={saving || !userId} type="submit">
          {saving ? "Saving..." : "Save transaction"}
        </Button>
      </form>
    </div>
  );
}

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}
