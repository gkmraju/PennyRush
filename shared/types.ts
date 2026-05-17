export type TransactionType = "income" | "expense" | "transfer";
export type TransactionSource = "manual" | "import" | "ocr" | "voice" | "sms";
export type AccountType = "bank" | "wallet" | "cash" | "credit_card";

export type CategoryName =
  | "Food"
  | "Transport"
  | "Shopping"
  | "Bills"
  | "Entertainment"
  | "Health"
  | "Education"
  | "Travel"
  | "Subscriptions"
  | "Income"
  | "Transfer"
  | "ATM"
  | "Investment"
  | "Personal Care"
  | "Groceries"
  | "Fuel"
  | "Rent"
  | "Other";

export type ImportCandidate = {
  id: string;
  date: string;
  merchant: string;
  amount: number;
  type: TransactionType;
  note?: string;
};

export type CategorizationRequestItem = {
  id: string;
  merchant: string;
  amount: number;
  note?: string;
};

export type CategorizationResult = {
  id: string;
  category: CategoryName;
  category_id?: string;
  confidence: number;
  source: "user" | "ai" | "rule" | "fallback";
};
