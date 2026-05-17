"use client";

import {
  BarChart3,
  Bell,
  CircleDollarSign,
  Home,
  Landmark,
  LineChart,
  PiggyBank,
  ReceiptText,
  Settings,
  Target,
} from "lucide-react";
import { cn } from "@/lib/utils";

const navItems = [
  { label: "Home", icon: Home, active: true },
  { label: "Transactions", icon: ReceiptText },
  { label: "Budgets", icon: BarChart3 },
  { label: "Goals", icon: Target },
  { label: "Subscriptions", icon: Bell },
  { label: "Net Worth", icon: LineChart },
  { label: "Accounts", icon: Landmark },
  { label: "Settings", icon: Settings },
];

export function AppSidebar() {
  return (
    <aside className="hidden min-h-screen w-72 shrink-0 border-r border-border/55 px-5 py-6 lg:block">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary text-primary-foreground">
          <CircleDollarSign className="h-6 w-6" aria-hidden="true" />
        </div>
        <div>
          <p className="text-base font-bold leading-tight">PennyRush</p>
          <p className="text-xs text-muted-foreground">Every penny, tracked</p>
        </div>
      </div>

      <nav className="mt-10 space-y-1" aria-label="Primary">
        {navItems.map((item) => (
          <button
            key={item.label}
            className={cn(
              "flex h-11 w-full items-center gap-3 rounded-chip px-3 text-sm font-semibold text-muted-foreground transition duration-300 ease-in-out hover:bg-muted hover:text-foreground",
              item.active && "bg-muted text-foreground",
            )}
            type="button"
          >
            <item.icon className="h-4 w-4" aria-hidden="true" />
            {item.label}
          </button>
        ))}
      </nav>

      <div className="mt-10 rounded-card bg-muted p-4">
        <PiggyBank className="h-5 w-5 text-primary" aria-hidden="true" />
        <p className="mt-3 text-sm font-semibold">Local-only mode</p>
        <p className="mt-1 text-xs leading-5 text-muted-foreground">AI stays off until you switch it on.</p>
      </div>
    </aside>
  );
}
