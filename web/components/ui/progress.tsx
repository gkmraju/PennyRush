import { cn } from "@/lib/utils";

export function Progress({ value, className }: { value: number; className?: string }) {
  const bounded = Math.min(100, Math.max(0, value));
  return (
    <div className={cn("h-2 w-full overflow-hidden rounded-full bg-muted", className)}>
      <div className="h-full rounded-full bg-primary transition-all duration-300 ease-in-out" style={{ width: `${bounded}%` }} />
    </div>
  );
}
