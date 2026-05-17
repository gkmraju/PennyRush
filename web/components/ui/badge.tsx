import * as React from "react";
import { cn } from "@/lib/utils";

export function Badge({ className, ...props }: React.HTMLAttributes<HTMLSpanElement>) {
  return (
    <span
      className={cn("inline-flex h-8 items-center rounded-chip bg-muted px-3 text-xs font-semibold text-muted-foreground", className)}
      {...props}
    />
  );
}
