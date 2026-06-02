"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import type { SupabaseClient } from "@supabase/supabase-js";
import { CheckCircle2, FileText, ShieldCheck, Sparkles, Upload, XCircle, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  enrichImportCandidates,
  saveImportCandidates,
  type CategoryRow,
  type ProfileSettings,
} from "@/lib/dashboard-data";
import { parseCsvImport, type ParsedImportCandidate } from "@/lib/parsers/csv";
import type { TransactionRow } from "@/lib/transactions";
import { formatCurrency } from "@/lib/utils";

type ImportStatus = "idle" | "parsed" | "empty" | "error" | "saving" | "saved";

export function ImportCard({
  supabase,
  userId,
  profile,
  categories,
  existingTransactions,
  openRequest,
  onImported,
}: {
  supabase: SupabaseClient;
  userId: string | null;
  profile: ProfileSettings;
  categories: CategoryRow[];
  existingTransactions: TransactionRow[];
  openRequest: number;
  onImported: () => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [fileName, setFileName] = useState<string | null>(null);
  const [rows, setRows] = useState<ParsedImportCandidate[]>([]);
  const [status, setStatus] = useState<ImportStatus>("idle");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (openRequest > 0) inputRef.current?.click();
  }, [openRequest]);

  const newRows = useMemo(() => rows.filter((row) => row.status !== "duplicate"), [rows]);
  const duplicateRows = rows.length - newRows.length;
  const income = useMemo(() => newRows.filter((row) => row.type === "income").reduce((sum, row) => sum + row.amount, 0), [newRows]);
  const expenses = useMemo(() => newRows.filter((row) => row.type === "expense").reduce((sum, row) => sum + row.amount, 0), [newRows]);

  async function handleFile(file: File) {
    setFileName(file.name);
    setRows([]);
    setMessage(null);

    if (!isCsv(file)) {
      setStatus("error");
      setMessage("Choose a CSV file exported from your bank.");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setStatus("error");
      setMessage("This file is larger than 5 MB. Export a shorter CSV range and try again.");
      return;
    }

    const text = await file.text();
    const parsed = parseCsvImport(text);
    const enriched = enrichImportCandidates(parsed.candidates, categories, existingTransactions);
    setRows(enriched);

    if (enriched.length === 0) {
      setStatus("empty");
      setMessage(parsed.errors[0] ?? "No transaction rows were detected.");
      return;
    }

    setStatus("parsed");
    setMessage(
      parsed.skippedRows > 0
        ? `${parsed.skippedRows} row${parsed.skippedRows === 1 ? "" : "s"} skipped because date or amount was missing.`
        : null,
    );
  }

  async function confirmImport() {
    if (!userId || newRows.length === 0) return;
    setStatus("saving");
    setMessage(null);

    try {
      const result = await saveImportCandidates(supabase, userId, profile, rows);
      setStatus("saved");
      setMessage(
        `Imported ${result.inserted.length} transaction${result.inserted.length === 1 ? "" : "s"}${
          result.duplicates > 0 ? ` and skipped ${result.duplicates} duplicate${result.duplicates === 1 ? "" : "s"}` : ""
        }.`,
      );
      onImported();
    } catch (error) {
      setStatus("parsed");
      setMessage(error instanceof Error ? error.message : "Could not save this import.");
    }
  }

  return (
    <Card className="min-h-96" id="statement-import">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-muted-foreground">Statement import</p>
          <h2 className="mt-2 text-xl font-bold">Review before saving</h2>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-primary/20 text-primary">
          <Upload className="h-5 w-5" aria-hidden="true" />
        </div>
      </div>

      <button
        className="mt-6 flex min-h-36 w-full flex-col items-center justify-center rounded-card border border-dashed border-border bg-muted/45 px-4 text-center transition duration-300 ease-in-out hover:bg-muted"
        onDragOver={(event) => event.preventDefault()}
        onDrop={(event) => {
          event.preventDefault();
          const file = event.dataTransfer.files[0];
          if (file) void handleFile(file);
        }}
        onClick={() => inputRef.current?.click()}
        type="button"
      >
        <FileText className="h-8 w-8 text-muted-foreground" aria-hidden="true" />
        <span className="mt-3 text-sm font-bold">{fileName ?? "Drop a CSV or pick a file"}</span>
        <span className="mt-2 max-w-xs text-xs leading-5 text-muted-foreground">
          Processed in memory and discarded, never stored.
        </span>
      </button>

      <input
        ref={inputRef}
        accept=".csv,text/csv"
        className="sr-only"
        onChange={(event) => {
          const file = event.currentTarget.files?.[0];
          event.currentTarget.value = "";
          if (file) void handleFile(file);
        }}
        type="file"
      />

      {status === "idle" ? (
        <div className="mt-5 grid grid-cols-3 gap-3 text-xs font-semibold text-muted-foreground">
          <Step icon={FileText} label="Map" />
          <Step icon={Sparkles} label="Categorize" />
          <Step icon={ShieldCheck} label="Confirm" />
        </div>
      ) : null}

      {status === "error" || status === "empty" ? (
        <Notice icon={XCircle} tone="error" message={message ?? "No transaction rows were detected."} />
      ) : null}

      {status === "saved" ? <Notice icon={CheckCircle2} tone="success" message={message ?? "Import complete."} /> : null}

      {status === "parsed" || status === "saving" ? (
        <div className="mt-5">
          <div className="mb-3 grid gap-2 text-sm sm:grid-cols-3">
            <SummaryPill label="New" value={String(newRows.length)} />
            <SummaryPill label="Income" value={formatCurrency(income, profile.currency, profile.locale)} />
            <SummaryPill label="Expenses" value={formatCurrency(expenses, profile.currency, profile.locale)} />
          </div>
          {duplicateRows > 0 ? (
            <p className="mb-3 rounded-card bg-muted px-3 py-2 text-xs font-semibold text-muted-foreground">
              {duplicateRows} duplicate{duplicateRows === 1 ? "" : "s"} already exist and will be skipped.
            </p>
          ) : null}
          {message ? (
            <p className="mb-3 rounded-card bg-muted px-3 py-2 text-xs text-muted-foreground">{message}</p>
          ) : null}
          <div className="max-h-56 overflow-auto rounded-card bg-muted/55">
            <table className="w-full text-left text-xs">
              <thead className="sticky top-0 bg-muted text-muted-foreground">
                <tr>
                  <th className="px-3 py-2 font-semibold">Date</th>
                  <th className="px-3 py-2 font-semibold">Merchant</th>
                  <th className="px-3 py-2 font-semibold">Category</th>
                  <th className="px-3 py-2 text-right font-semibold">Amount</th>
                </tr>
              </thead>
              <tbody>
                {rows.slice(0, 10).map((row) => {
                  const signed = row.type === "income" ? row.amount : -row.amount;
                  return (
                    <tr key={row.id} className={row.status === "duplicate" ? "text-muted-foreground line-through" : undefined}>
                      <td className="px-3 py-2 tabular">{row.date}</td>
                      <td className="max-w-36 truncate px-3 py-2">{row.merchant}</td>
                      <td className="max-w-28 truncate px-3 py-2">{row.category ?? "Other"}</td>
                      <td className="px-3 py-2 text-right tabular">{formatCurrency(signed, profile.currency, profile.locale)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <Button className="mt-4 w-full" disabled={!userId || newRows.length === 0 || status === "saving"} onClick={confirmImport} type="button">
            {status === "saving" ? "Saving..." : `Confirm ${newRows.length} new row${newRows.length === 1 ? "" : "s"}`}
          </Button>
        </div>
      ) : null}
    </Card>
  );
}

function Step({
  icon: Icon,
  label,
}: {
  icon: LucideIcon;
  label: string;
}) {
  return (
    <span className="flex h-10 items-center justify-center gap-2 rounded-chip bg-muted">
      <Icon className="h-4 w-4" aria-hidden={true} />
      {label}
    </span>
  );
}

function SummaryPill({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-card bg-muted px-3 py-2">
      <p className="text-sm font-semibold text-muted-foreground">{label}</p>
      <p className="tabular mt-1 text-sm font-bold">{value}</p>
    </div>
  );
}

function Notice({
  icon: Icon,
  tone,
  message,
}: {
  icon: LucideIcon;
  tone: "error" | "success";
  message: string;
}) {
  return (
    <div className="mt-5 flex items-start gap-3 rounded-card bg-muted p-4 text-sm text-muted-foreground">
      <Icon className={`mt-0.5 h-4 w-4 ${tone === "success" ? "text-success" : "text-danger"}`} aria-hidden="true" />
      <p className="leading-6">{message}</p>
    </div>
  );
}

function isCsv(file: File) {
  return file.name.toLowerCase().endsWith(".csv") || file.type.includes("csv") || file.type === "text/plain";
}
