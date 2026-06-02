export type TransactionRow = {
  id: string;
  amount: number;
  type: "income" | "expense" | "transfer";
  date: string;
  merchant: string | null;
  note: string | null;
  source: "manual" | "import" | "ocr" | "voice" | "sms";
  created_at: string;
  category_id: string | null;
  imported_hash: string | null;
  category: {
    name: string;
    color: string | null;
  } | null;
};

export type DashboardTransaction = {
  id: string;
  merchant: string;
  category: string;
  amount: number;
  date: string;
  color: string;
};

export type SpendingSlice = {
  name: string;
  value: number;
  color: string;
};

export type DashboardMetrics = {
  netWorth: number;
  income: number;
  expenses: number;
  saved: number;
};

const palette = ["#FDE68A", "#BFDBFE", "#FBCFE8", "#DDD6FE", "#FED7AA", "#BBF7D0"];

export function buildMetrics(rows: TransactionRow[]): DashboardMetrics {
  const thisMonth = rows.filter((row) => isThisMonth(row.date));
  const income = thisMonth.filter((row) => row.type === "income").reduce((sum, row) => sum + Number(row.amount), 0);
  const expenses = thisMonth.filter((row) => row.type === "expense").reduce((sum, row) => sum + Number(row.amount), 0);
  const netWorth = rows.reduce((sum, row) => sum + signedAmount(row), 0);

  return {
    netWorth,
    income,
    expenses,
    saved: income - expenses,
  };
}

export function buildRecentTransactions(rows: TransactionRow[]): DashboardTransaction[] {
  return rows.slice(0, 8).map((row, index) => ({
    id: row.id,
    merchant: row.merchant?.trim() || row.note?.trim() || "Transaction",
    category: row.category?.name ?? labelForSource(row.source),
    amount: signedAmount(row),
    date: formatRelativeDate(row.date),
    color: row.category?.color ?? palette[index % palette.length],
  }));
}

export function buildSpending(rows: TransactionRow[]): SpendingSlice[] {
  const buckets = new Map<string, { value: number; color: string | null }>();

  for (const row of rows) {
    if (row.type !== "expense" || !isThisMonth(row.date)) continue;
    const key = row.category?.name ?? row.merchant?.trim() ?? row.note?.trim() ?? "Other";
    const current = buckets.get(key);
    buckets.set(key, {
      value: (current?.value ?? 0) + Number(row.amount),
      color: current?.color ?? row.category?.color ?? null,
    });
  }

  return [...buckets.entries()]
    .sort((a, b) => b[1].value - a[1].value)
    .slice(0, 6)
    .map(([name, item], index) => ({
      name,
      value: item.value,
      color: item.color ?? palette[index % palette.length],
    }));
}

function signedAmount(row: TransactionRow) {
  const amount = Number(row.amount);
  if (row.type === "expense") return -amount;
  return amount;
}

function isThisMonth(value: string) {
  const date = new Date(`${value}T00:00:00`);
  const now = new Date();
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
}

function formatRelativeDate(value: string) {
  const date = new Date(`${value}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const diffDays = Math.round((today.getTime() - date.getTime()) / 86_400_000);
  if (diffDays === 0) return "Today";
  if (diffDays === 1) return "Yesterday";

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
  }).format(date);
}

function labelForSource(source: TransactionRow["source"]) {
  if (source === "import") return "Imported";
  if (source === "ocr") return "Receipt";
  if (source === "voice") return "Voice";
  if (source === "sms") return "SMS";
  return "Manual";
}
