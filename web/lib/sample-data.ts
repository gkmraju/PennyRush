export const metrics = {
  netWorth: 48210,
  monthBalance: 1840,
  income: 6400,
  expenses: 4560,
  saved: 1840,
};

export const spending = [
  { name: "Food", value: 920, color: "#F5B82E" },
  { name: "Rent", value: 1700, color: "#A3A3A3" },
  { name: "Transport", value: 380, color: "#60A5FA" },
  { name: "Shopping", value: 640, color: "#F9A8D4" },
  { name: "Bills", value: 520, color: "#A78BFA" },
  { name: "Other", value: 400, color: "#34D399" },
];

export const insights = [
  {
    title: "Dining is running hot",
    body: "Food spend is 28% above your 3-month average.",
    severity: "warning",
  },
  {
    title: "Goal pace improved",
    body: "You are 9 days ahead on the emergency fund.",
    severity: "success",
  },
  {
    title: "Two bills land this week",
    body: "Set aside $164 for recurring payments.",
    severity: "info",
  },
];

export const transactions = [
  { id: "tx_1", merchant: "Whole Foods", category: "Groceries", amount: -84, date: "Today", color: "#D9F99D" },
  { id: "tx_2", merchant: "Payroll", category: "Income", amount: 3200, date: "Today", color: "#BBF7D0" },
  { id: "tx_3", merchant: "Uber", category: "Transport", amount: -24, date: "Yesterday", color: "#BFDBFE" },
  { id: "tx_4", merchant: "Netflix", category: "Subscriptions", amount: -18, date: "Yesterday", color: "#E9D5FF" },
  { id: "tx_5", merchant: "Blue Bottle", category: "Food", amount: -12, date: "Fri", color: "#FDE68A" },
];
