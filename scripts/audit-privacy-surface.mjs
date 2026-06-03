import { existsSync, readFileSync } from "node:fs";

const files = [
  "android/app/src/main/AndroidManifest.xml",
  "android/gradle/libs.versions.toml",
  "android/app/build.gradle.kts",
  "android/core/common/build.gradle.kts",
  "android/core/database-cache/build.gradle.kts",
  "android/core/designsystem/build.gradle.kts",
  "android/core/network/build.gradle.kts",
  "android/core/security/build.gradle.kts",
  "android/feature/home/build.gradle.kts",
  "android/feature/insights/build.gradle.kts",
  "android/feature/onboarding/build.gradle.kts",
  "android/feature/statements/build.gradle.kts",
  "android/feature/transactions/build.gradle.kts",
  "package.json",
  "web/package.json",
];

const allowedAndroidPermissions = new Set([
  "android.permission.INTERNET",
  "android.permission.POST_NOTIFICATIONS",
]);

const blockedAndroidPermissions = [
  "android.permission.ACCESS_FINE_LOCATION",
  "android.permission.ACCESS_COARSE_LOCATION",
  "android.permission.READ_SMS",
  "android.permission.RECEIVE_SMS",
  "android.permission.SEND_SMS",
  "android.permission.READ_CONTACTS",
  "android.permission.GET_ACCOUNTS",
  "android.permission.RECORD_AUDIO",
  "android.permission.READ_MEDIA_IMAGES",
  "android.permission.READ_MEDIA_VIDEO",
  "android.permission.READ_EXTERNAL_STORAGE",
  "android.permission.WRITE_EXTERNAL_STORAGE",
];

const blockedDependencyPatterns = [
  /firebase-(analytics|crashlytics|perf|messaging)/i,
  /google\.firebase:firebase-(analytics|crashlytics|perf|messaging)/i,
  /play-services-(ads|analytics|location)/i,
  /com\.google\.android\.gms:play-services-(ads|analytics|location)/i,
  /facebook/i,
  /appsflyer/i,
  /adjust/i,
  /mixpanel/i,
  /amplitude/i,
  /segment/i,
  /sentry/i,
];

const failures = [];

function read(path) {
  if (!existsSync(path)) {
    failures.push(`Missing privacy audit input: ${path}`);
    return "";
  }

  return readFileSync(path, "utf8");
}

const manifest = read("android/app/src/main/AndroidManifest.xml");
const permissionMatches = [
  ...manifest.matchAll(/<uses-permission\s+[^>]*android:name="([^"]+)"/g),
].map((match) => match[1]);

for (const permission of permissionMatches) {
  if (!allowedAndroidPermissions.has(permission)) {
    failures.push(`Unexpected Android permission declared: ${permission}`);
  }
}

for (const permission of blockedAndroidPermissions) {
  if (manifest.includes(permission)) {
    failures.push(`Blocked Android permission declared: ${permission}`);
  }
}

for (const permission of allowedAndroidPermissions) {
  if (!permissionMatches.includes(permission)) {
    failures.push(`Expected Android permission is missing: ${permission}`);
  }
}

for (const file of files) {
  const body = read(file);
  for (const pattern of blockedDependencyPatterns) {
    if (pattern.test(body)) {
      failures.push(`${file} includes blocked tracking or ad dependency pattern: ${pattern}`);
    }
  }
}

const versions = read("android/gradle/libs.versions.toml");
if (!versions.includes("com.google.mlkit:text-recognition")) {
  failures.push("Receipt scan dependency is missing: com.google.mlkit:text-recognition");
}

if (!versions.includes("io.github.jan-tennert.supabase:bom")) {
  failures.push("Supabase dependency marker is missing.");
}

if (failures.length > 0) {
  console.error("Privacy surface audit failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log("Privacy surface audit passed.");
