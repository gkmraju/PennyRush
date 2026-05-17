"use client";

import { BarChart3, Home, Lightbulb, Plus, ReceiptText, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";

export function MobileNav({ onAdd }: { onAdd: () => void }) {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-30 border-t border-border/65 bg-background/92 px-4 py-3 backdrop-blur lg:hidden" aria-label="Mobile">
      <div className="mx-auto grid max-w-md grid-cols-5 items-center gap-2">
        <MobileNavItem icon={Home} label="Home" active />
        <MobileNavItem icon={ReceiptText} label="Txns" />
        <Button className="mx-auto h-12 w-12 rounded-fab p-0" onClick={onAdd} aria-label="Add transaction">
          <Plus className="h-5 w-5" aria-hidden="true" />
        </Button>
        <MobileNavItem icon={Lightbulb} label="Insights" />
        <MobileNavItem icon={BarChart3} label="More" />
      </div>
    </nav>
  );
}

function MobileNavItem({
  icon: Icon,
  label,
  active,
}: {
  icon: LucideIcon;
  label: string;
  active?: boolean;
}) {
  return (
    <button
      className="flex min-h-11 flex-col items-center justify-center gap-1 rounded-chip text-[11px] font-semibold text-muted-foreground data-[active=true]:text-foreground"
      data-active={active}
      type="button"
    >
      <Icon className="h-4 w-4" aria-hidden={true} />
      {label}
    </button>
  );
}
