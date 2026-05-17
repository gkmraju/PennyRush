"use client";

import { useMemo, useRef, useState } from "react";
import { FileText, ShieldCheck, Sparkles, Upload, type LucideIcon } from "lucide-react";
import type { ImportCandidate } from "@pennyrush/shared";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { parseCsvTransactions } from "@/lib/parsers/csv";
import { formatCurrency } from "@/lib/utils";

export function ImportCard() {
  const inputRef = useRef<HTMLInputElement>(null);
  const [fileName, setFileName] = useState<string | null>(null);
  const [rows, setRows] = useState<ImportCandidate[]>([]);
  const [status, setStatus] = useState<"idle" | "parsed" | "empty">("idle");

  const total = useMemo(() => rows.reduce((sum, row) => sum + row.amount, 0), [rows]);

  async function handleFile(file: File) {
    setFileName(file.name);
    const text = await file.text();
    const parsed = parseCsvTransactions(text);
    setRows(parsed);
    setStatus(parsed.length > 0 ? "parsed" : "empty");
  }

  return (
    <Card className="min-h-96">
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
        onClick={() => inputRef.current?.click()}
        type="button"
      >
        <FileText className="h-8 w-8 text-muted-foreground" aria-hidden="true" />
        <span className="mt-3 text-sm font-bold">{fileName ?? "Drop a CSV or pick a file"}</span>
        <span className="mt-2 max-w-xs text-xs leading-5 text-muted-foreground">
          Processed in memory and discarded -- never stored.
        </span>
      </button>

      <input
        ref={inputRef}
        accept=".csv,text/csv"
        className="sr-only"
        onChange={(event) => {
          const file = event.currentTarget.files?.[0];
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

      {status === "empty" ? (
        <p className="mt-5 rounded-card bg-muted p-4 text-sm text-muted-foreground">
          No transaction rows were detected. You can remap columns in the next import slice.
        </p>
      ) : null}

      {status === "parsed" ? (
        <div className="mt-5">
          <div className="mb-3 flex items-center justify-between text-sm">
            <span className="font-bold">{rows.length} rows detected</span>
            <span className="tabular text-muted-foreground">{formatCurrency(total)}</span>
          </div>
          <div className="max-h-48 overflow-auto rounded-card bg-muted/55">
            <table className="w-full text-left text-xs">
              <thead className="sticky top-0 bg-muted text-muted-foreground">
                <tr>
                  <th className="px-3 py-2 font-semibold">Date</th>
                  <th className="px-3 py-2 font-semibold">Merchant</th>
                  <th className="px-3 py-2 text-right font-semibold">Amount</th>
                </tr>
              </thead>
              <tbody>
                {rows.slice(0, 8).map((row) => (
                  <tr key={row.id}>
                    <td className="px-3 py-2 tabular">{row.date}</td>
                    <td className="max-w-40 truncate px-3 py-2">{row.merchant}</td>
                    <td className="px-3 py-2 text-right tabular">{formatCurrency(row.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Button className="mt-4 w-full" type="button">
            Confirm import
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
