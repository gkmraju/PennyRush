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
  { label: "Home", icon: Home, key: "home" },
  { label: "Activity", icon: ReceiptText, key: "activity" },
  { label: "Plan", icon: BarChart3, key: "plan" },
  { label: "Goals", icon: Target, key: "plan" },
  { label: "Insights", icon: Bell, key: "insights" },
  { label: "Net Worth", icon: LineChart, key: "home" },
  { label: "Accounts", icon: Landmark, key: "account" },
  { label: "Settings", icon: Settings, key: "account" },
];

export type SidebarDestination = "home" | "activity" | "plan" | "insights" | "account";

export function AppSidebar({
  active = "home",
  onNavigate,
}: {
  active?: SidebarDestination;
  onNavigate?: (destination: SidebarDestination) => void;
}) {
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
              active === item.key && "bg-muted text-foreground",
            )}
            onClick={() => onNavigate?.(item.key as SidebarDestination)}
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
