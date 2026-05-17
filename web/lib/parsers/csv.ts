import type { ImportCandidate } from "@pennyrush/shared";

type Mapping = {
  date: number;
  merchant: number;
  amount: number;
  type?: number;
};

export function parseCsvTransactions(text: string): ImportCandidate[] {
  const rows = parseRows(text);
  if (rows.length < 2) return [];

  const headers = rows[0].map((header) => header.toLowerCase().trim());
  const mapping: Mapping = {
    date: findHeader(headers, ["date", "transaction date", "posted date"]),
    merchant: findHeader(headers, ["merchant", "description", "narration", "details"]),
    amount: findHeader(headers, ["amount", "debit", "credit", "withdrawal", "deposit"]),
    type: findHeader(headers, ["type", "direction"]),
  };

  if (mapping.date < 0 || mapping.merchant < 0 || mapping.amount < 0) return [];

  return rows.slice(1).flatMap((row, index) => {
    const amount = Math.abs(Number(String(row[mapping.amount] ?? "0").replace(/,/g, "")));
    if (!Number.isFinite(amount) || amount === 0) return [];

    const typeText = mapping.type === undefined || mapping.type < 0 ? "" : String(row[mapping.type]).toLowerCase();
    const type = typeText.includes("credit") || typeText.includes("income") ? "income" : "expense";

    return {
      id: `import_${Date.now()}_${index}`,
      date: normalizeDate(row[mapping.date]),
      merchant: row[mapping.merchant] || `Imported row ${index + 1}`,
      amount,
      type,
      note: "Client-side CSV import",
    };
  });
}

function parseRows(text: string) {
  const rows: string[][] = [];
  let row: string[] = [];
  let cell = "";
  let quoted = false;

  for (let index = 0; index < text.length; index += 1) {
    const char = text[index];
    const next = text[index + 1];

    if (char === '"' && next === '"') {
      cell += '"';
      index += 1;
    } else if (char === '"') {
      quoted = !quoted;
    } else if (char === "," && !quoted) {
      row.push(cell.trim());
      cell = "";
    } else if ((char === "\n" || char === "\r") && !quoted) {
      if (char === "\r" && next === "\n") index += 1;
      row.push(cell.trim());
      if (row.some(Boolean)) rows.push(row);
      row = [];
      cell = "";
    } else {
      cell += char;
    }
  }

  row.push(cell.trim());
  if (row.some(Boolean)) rows.push(row);
  return rows;
}

function findHeader(headers: string[], candidates: string[]) {
  return headers.findIndex((header) => candidates.includes(header));
}

function normalizeDate(value: string) {
  const parsed = new Date(value);
  return Number.isNaN(parsed.valueOf()) ? new Date().toISOString().slice(0, 10) : parsed.toISOString().slice(0, 10);
}
