"use client";

import { BarChart3, Home, Lightbulb, Plus, ReceiptText, Settings, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { SidebarDestination } from "@/components/features/app-sidebar";

const navItems: Array<{ label: string; icon: LucideIcon; key: SidebarDestination }> = [
  { label: "Home", icon: Home, key: "home" },
  { label: "Activity", icon: ReceiptText, key: "activity" },
  { label: "Plan", icon: BarChart3, key: "plan" },
  { label: "Insights", icon: Lightbulb, key: "insights" },
  { label: "Account", icon: Settings, key: "account" },
];

export function MobileNav({
  active = "home",
  onAdd,
  onNavigate,
}: {
  active?: SidebarDestination;
  onAdd: () => void;
  onNavigate?: (destination: SidebarDestination) => void;
}) {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-30 border-t border-border/65 bg-background/92 px-4 py-3 backdrop-blur lg:hidden" aria-label="Mobile">
      <Button className="absolute -top-14 right-4 h-12 w-12 rounded-fab p-0 shadow-soft" onClick={onAdd} aria-label="Add entry">
        <Plus className="h-5 w-5" aria-hidden="true" />
      </Button>
      <div className="mx-auto grid max-w-md grid-cols-5 items-center gap-2">
        {navItems.map((item) => (
          <MobileNavItem
            key={item.label}
            active={active === item.key}
            icon={item.icon}
            label={item.label}
            onClick={() => onNavigate?.(item.key)}
          />
        ))}
      </div>
    </nav>
  );
}

function MobileNavItem({
  icon: Icon,
  label,
  active,
  onClick,
}: {
  icon: LucideIcon;
  label: string;
  active?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      className="flex min-h-12 flex-col items-center justify-center gap-1 rounded-chip text-xs font-semibold text-muted-foreground data-[active=true]:text-foreground"
      data-active={active}
      onClick={onClick}
      type="button"
    >
      <Icon className="h-4 w-4" aria-hidden={true} />
      {label}
    </button>
  );
}
