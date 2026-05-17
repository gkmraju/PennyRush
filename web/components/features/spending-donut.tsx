"use client";

import { Pie, PieChart, ResponsiveContainer, Cell, Tooltip } from "recharts";
import { Card } from "@/components/ui/card";
import { spending } from "@/lib/sample-data";
import { formatCurrency } from "@/lib/utils";

export function SpendingDonut() {
  const total = spending.reduce((sum, item) => sum + item.value, 0);

  return (
    <Card className="min-h-96">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-muted-foreground">Spending</p>
          <h2 className="mt-2 text-xl font-bold">May categories</h2>
        </div>
        <p className="tabular rounded-chip bg-muted px-3 py-2 text-sm font-bold">{formatCurrency(total)}</p>
      </div>

      <div className="mt-6 h-56">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={spending} dataKey="value" nameKey="name" innerRadius={68} outerRadius={94} paddingAngle={3}>
              {spending.map((item) => (
                <Cell key={item.name} fill={item.color} stroke="transparent" />
              ))}
            </Pie>
            <Tooltip formatter={(value) => formatCurrency(Number(value))} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3">
        {spending.slice(0, 4).map((item) => (
          <div key={item.name} className="flex items-center gap-2 text-sm">
            <span className="h-3 w-3 rounded-full" style={{ backgroundColor: item.color }} />
            <span className="text-muted-foreground">{item.name}</span>
            <span className="tabular ml-auto font-semibold">{formatCurrency(item.value)}</span>
          </div>
        ))}
      </div>
    </Card>
  );
}
