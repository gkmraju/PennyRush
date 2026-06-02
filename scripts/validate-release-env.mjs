const required = [
  "NEXT_PUBLIC_SUPABASE_URL",
  "NEXT_PUBLIC_SUPABASE_ANON_KEY",
  "PENNYRUSH_SUPABASE_URL",
  "PENNYRUSH_SUPABASE_ANON_KEY",
  "SUPABASE_SERVICE_ROLE_KEY",
  "PENNYRUSH_RELEASE_STORE_PASSWORD",
  "PENNYRUSH_RELEASE_KEY_ALIAS",
  "PENNYRUSH_RELEASE_KEY_PASSWORD",
];

const missing = required.filter((key) => !process.env[key]?.trim());
const hasLocalKeystore = Boolean(process.env.PENNYRUSH_RELEASE_STORE_FILE?.trim());
const hasCiKeystore = Boolean(process.env.PENNYRUSH_RELEASE_KEYSTORE_BASE64?.trim());

if (!hasLocalKeystore && !hasCiKeystore) {
  missing.push("PENNYRUSH_RELEASE_STORE_FILE or PENNYRUSH_RELEASE_KEYSTORE_BASE64");
}

if (missing.length > 0) {
  console.error("Missing required release environment values:");
  for (const key of missing) {
    console.error(`- ${key}`);
  }
  process.exit(1);
}

console.log("Release environment values are present.");
