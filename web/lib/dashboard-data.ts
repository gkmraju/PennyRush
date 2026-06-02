import type { SupabaseClient } from "@supabase/supabase-js";
import type { TransactionSource, TransactionType } from "@pennyrush/shared";
import { categorizeLocally } from "@/lib/ai/rules";
import {
  buildImportDedupeKey,
  buildTransactionInsertPayload,
  type ExistingTransactionLike,
  type ParsedImportCandidate,
} from "@/lib/parsers/csv";
import type { TransactionRow } from "@/lib/transactions";

export type ProfileSettings = {
  currency: string;
  locale: string;
  localOnlyMode: boolean;
};

export type AccountRow = {
  id: string;
  name: string;
  currency: string;
};

export type CategoryRow = {
  id: string;
  name: string;
  color: string;
  icon: string;
};

export type InsightRow = {
  id: string;
  title: string;
  body: string;
  severity: "info" | "success" | "warning" | "critical";
  type: string;
  action_url: string | null;
  created_at: string;
};

export type DashboardData = {
  userId: string;
  profile: ProfileSettings;
  account: AccountRow | null;
  categories: CategoryRow[];
  insights: InsightRow[];
  transactions: TransactionRow[];
};

export type ImportSaveResult = {
  inserted: TransactionRow[];
  duplicates: number;
};

export type ManualTransactionInput = {
  amount: number;
  type: TransactionType;
  date: string;
  merchant: string;
  note?: string;
  category_id?: string | null;
};

type RawTransactionRow = Omit<TransactionRow, "category"> & {
  category_id: string | null;
  imported_hash: string | null;
  categories?: { name?: string | null; color?: string | null } | { name?: string | null; color?: string | null }[] | null;
};

const transactionSelect =
  "id, amount, type, date, merchant, note, source, created_at, category_id, imported_hash, categories(name,color)";

const defaultProfile: ProfileSettings = {
  currency: "USD",
  locale: "en-US",
  localOnlyMode: false,
};

export async function loadDashboardData(supabase: SupabaseClient): Promise<DashboardData> {
  const {
    data: { user },
    error: userError,
  } = await supabase.auth.getUser();

  if (userError || !user) {
    throw new Error(userError?.message || "You need to sign in again.");
  }

  const [{ data: profile }, { data: accounts }, { data: categories }, { data: transactions }, { data: insights }] =
    await Promise.all([
      supabase.from("profiles").select("currency, locale, local_only_mode").eq("id", user.id).maybeSingle(),
      supabase
        .from("accounts")
        .select("id, name, currency")
        .eq("user_id", user.id)
        .eq("archived", false)
        .order("created_at", { ascending: true })
        .limit(1),
      supabase.from("categories").select("id, name, color, icon").eq("user_id", user.id).order("name"),
      supabase
        .from("transactions")
        .select(transactionSelect)
        .order("date", { ascending: false })
        .order("created_at", { ascending: false })
        .limit(500),
      supabase
        .from("insights")
        .select("id, title, body, severity, type, action_url, created_at")
        .eq("dismissed", false)
        .order("created_at", { ascending: false })
        .limit(8),
    ]);

  return {
    userId: user.id,
    profile: normalizeProfile(profile),
    account: (accounts?.[0] as AccountRow | undefined) ?? null,
    categories: (categories ?? []) as CategoryRow[],
    insights: (insights ?? []) as InsightRow[],
    transactions: normalizeTransactionRows((transactions ?? []) as RawTransactionRow[]),
  };
}

export function enrichImportCandidates(
  candidates: ParsedImportCandidate[],
  categories: CategoryRow[],
  existingRows: ExistingTransactionLike[],
): ParsedImportCandidate[] {
  const existing = new Set(existingRows.map((row) => buildImportDedupeKey(row)));
  const seenInFile = new Set<string>();

  return candidates.map((candidate) => {
    const category = categoryForMerchant(candidate.merchant, categories);
    const duplicate = existing.has(candidate.dedupeKey) || seenInFile.has(candidate.dedupeKey);
    seenInFile.add(candidate.dedupeKey);

    return {
      ...candidate,
      category: category?.name ?? "Other",
      category_id: category?.id ?? null,
      confidence: category ? 0.86 : 0.35,
      status: duplicate ? "duplicate" : "new",
    };
  });
}

export async function saveImportCandidates(
  supabase: SupabaseClient,
  userId: string,
  profile: ProfileSettings,
  candidates: ParsedImportCandidate[],
): Promise<ImportSaveResult> {
  const account = await ensurePrimaryAccount(supabase, userId, profile);
  const eligible = candidates.filter((candidate) => candidate.status !== "duplicate");
  const existingHashes = await existingImportedHashes(supabase, userId, eligible.map((candidate) => candidate.importedHash));
  const payload = eligible
    .filter((candidate) => !existingHashes.has(candidate.importedHash))
    .map((candidate) => buildTransactionInsertPayload(candidate, userId, account.id));

  if (payload.length === 0) {
    return { inserted: [], duplicates: candidates.length };
  }

  const { data, error } = await supabase.from("transactions").insert(payload).select(transactionSelect);
  if (error) throw new Error(error.message);

  return {
    inserted: normalizeTransactionRows((data ?? []) as RawTransactionRow[]),
    duplicates: candidates.length - payload.length,
  };
}

export async function saveManualTransaction(
  supabase: SupabaseClient,
  userId: string,
  profile: ProfileSettings,
  categories: CategoryRow[],
  input: ManualTransactionInput,
) {
  const account = await ensurePrimaryAccount(supabase, userId, profile);
  const category = input.category_id ? categories.find((item) => item.id === input.category_id) : categoryForMerchant(input.merchant, categories);
  const payload = {
    user_id: userId,
    account_id: account.id,
    category_id: category?.id ?? null,
    amount: Math.abs(input.amount),
    type: input.type,
    date: input.date,
    merchant: input.merchant.trim(),
    note: input.note?.trim() || null,
    source: "manual" satisfies TransactionSource,
    ai_confidence: category ? 0.86 : null,
  };

  const { data, error } = await supabase.from("transactions").insert(payload).select(transactionSelect).single();
  if (error) throw new Error(error.message);
  return normalizeTransactionRows([data as RawTransactionRow])[0];
}

export function categoryForMerchant(merchant: string, categories: CategoryRow[]) {
  const categoryName = categorizeLocally(merchant);
  return (
    categories.find((category) => category.name.toLowerCase() === categoryName.toLowerCase()) ??
    categories.find((category) => category.name.toLowerCase() === "other") ??
    null
  );
}

export async function ensurePrimaryAccount(
  supabase: SupabaseClient,
  userId: string,
  profile: ProfileSettings,
): Promise<AccountRow> {
  const { data: existing, error: existingError } = await supabase
    .from("accounts")
    .select("id, name, currency")
    .eq("user_id", userId)
    .eq("archived", false)
    .order("created_at", { ascending: true })
    .limit(1);

  if (existingError) throw new Error(existingError.message);
  if (existing?.[0]) return existing[0] as AccountRow;

  const { data: inserted, error: insertError } = await supabase
    .from("accounts")
    .insert({
      user_id: userId,
      name: "Primary",
      type: "bank",
      currency: profile.currency,
      color: "#10B981",
      icon: "bank",
    })
    .select("id, name, currency")
    .single();

  if (insertError) throw new Error(insertError.message);
  return inserted as AccountRow;
}

function normalizeProfile(value: unknown): ProfileSettings {
  const row = value as { currency?: string | null; locale?: string | null; local_only_mode?: boolean | null } | null;
  return {
    currency: row?.currency || defaultProfile.currency,
    locale: row?.locale || defaultProfile.locale,
    localOnlyMode: Boolean(row?.local_only_mode),
  };
}

function normalizeTransactionRows(rows: RawTransactionRow[]): TransactionRow[] {
  return rows.map((row) => {
    const category = Array.isArray(row.categories) ? row.categories[0] : row.categories;

    return {
      id: row.id,
      amount: Number(row.amount),
      type: row.type,
      date: row.date,
      merchant: row.merchant,
      note: row.note,
      source: row.source,
      created_at: row.created_at,
      category_id: row.category_id,
      imported_hash: row.imported_hash,
      category: category?.name
        ? {
            name: category.name,
            color: category.color ?? null,
          }
        : null,
    };
  });
}

async function existingImportedHashes(supabase: SupabaseClient, userId: string, hashes: string[]) {
  if (hashes.length === 0) return new Set<string>();

  const { data, error } = await supabase
    .from("transactions")
    .select("imported_hash")
    .eq("user_id", userId)
    .in("imported_hash", hashes);

  if (error) throw new Error(error.message);
  return new Set((data ?? []).map((row: { imported_hash: string | null }) => row.imported_hash).filter(Boolean) as string[]);
}
