import { ArrowDownLeft, ArrowUpRight } from "lucide-react";
import { transactions } from "@/lib/sample-data";
import { formatCurrency } from "@/lib/utils";

export function RecentTransactions() {
  return (
    <section>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-bold">Recent transactions</h2>
        <button className="text-sm font-semibold text-muted-foreground" type="button">
          See all
        </button>
      </div>
      <div className="space-y-3">
        {transactions.map((transaction) => {
          const positive = transaction.amount > 0;
          const Icon = positive ? ArrowDownLeft : ArrowUpRight;
          return (
            <button
              key={transaction.id}
              className="grid min-h-16 w-full grid-cols-[44px_1fr_auto] items-center gap-3 rounded-card px-2 text-left transition duration-300 ease-in-out hover:bg-muted"
              type="button"
            >
              <span className="flex h-11 w-11 items-center justify-center rounded-2xl" style={{ backgroundColor: transaction.color }}>
                <Icon className="h-4 w-4 text-neutral-950" aria-hidden="true" />
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm font-bold">{transaction.merchant}</span>
                <span className="mt-1 block truncate text-xs text-muted-foreground">
                  {transaction.category} · {transaction.date}
                </span>
              </span>
              <span className={`tabular text-sm font-bold ${positive ? "text-success" : "text-foreground"}`}>
                {positive ? "+" : ""}
                {formatCurrency(transaction.amount)}
              </span>
            </button>
          );
        })}
      </div>
    </section>
  );
}
