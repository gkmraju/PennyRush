import * as React from "react";
import { cn } from "@/lib/utils";

export function Badge({ className, ...props }: React.HTMLAttributes<HTMLSpanElement>) {
  return (
    <span
      className={cn("inline-flex min-h-8 items-center rounded-chip bg-muted px-3 text-sm font-semibold text-muted-foreground", className)}
      {...props}
    />
  );
}
