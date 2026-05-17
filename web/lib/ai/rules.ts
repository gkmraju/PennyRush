import type { CategoryName } from "@pennyrush/shared";

const rules: Array<{ category: CategoryName; terms: string[] }> = [
  { category: "Food", terms: ["swiggy", "zomato", "restaurant", "cafe", "coffee"] },
  { category: "Groceries", terms: ["grocery", "supermarket", "whole foods", "bigbasket", "blinkit"] },
  { category: "Transport", terms: ["uber", "ola", "metro", "taxi", "lyft"] },
  { category: "Subscriptions", terms: ["netflix", "spotify", "icloud", "subscription"] },
  { category: "Fuel", terms: ["fuel", "petrol", "shell"] },
  { category: "Bills", terms: ["electricity", "broadband", "water bill", "phone bill"] },
  { category: "Investment", terms: ["zerodha", "groww", "dividend", "mutual fund"] },
];

export function categorizeLocally(merchant: string): CategoryName {
  const normalized = merchant.toLowerCase();
  return rules.find((rule) => rule.terms.some((term) => normalized.includes(term)))?.category ?? "Other";
}
