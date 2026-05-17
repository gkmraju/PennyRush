import * as React from "react";
import { cn } from "@/lib/utils";

export function Card({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("rounded-card bg-card p-5 text-card-foreground shadow-soft ring-1 ring-border/55", className)}
      {...props}
    />
  );
}
