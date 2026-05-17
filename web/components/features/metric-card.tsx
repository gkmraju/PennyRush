import { ArrowDownRight, ArrowUpRight, Minus } from "lucide-react";
import { Card } from "@/components/ui/card";
import { formatCurrency } from "@/lib/utils";

export function MetricCard({
  label,
  value,
  delta,
  tone,
}: {
  label: string;
  value: number;
  delta: string;
  tone: "income" | "expense" | "neutral";
}) {
  const Icon = tone === "income" ? ArrowUpRight : tone === "expense" ? ArrowDownRight : Minus;
  const toneClass = tone === "income" ? "text-success" : tone === "expense" ? "text-danger" : "text-muted-foreground";

  return (
    <Card className="min-h-32">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-muted-foreground">{label}</p>
          <p className="tabular mt-3 text-2xl font-bold tracking-normal">{formatCurrency(value)}</p>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-muted">
          <Icon className={`h-5 w-5 ${toneClass}`} aria-hidden="true" />
        </div>
      </div>
      <p className="mt-4 text-xs font-semibold text-muted-foreground">{delta}</p>
    </Card>
  );
}
