import { existsSync, readFileSync, statSync } from "node:fs";
import { spawnSync } from "node:child_process";
import { join } from "node:path";

const appId = "dev.pennyrush.app";
const versionName = "1.0.0";
const versionCode = 1;

const signingEnv = [
  "PENNYRUSH_RELEASE_STORE_FILE",
  "PENNYRUSH_RELEASE_STORE_PASSWORD",
  "PENNYRUSH_RELEASE_KEY_ALIAS",
  "PENNYRUSH_RELEASE_KEY_PASSWORD",
];
const hasReleaseSigning = signingEnv.every((name) => process.env[name]);
const requireSigned =
  process.env.PENNYRUSH_REQUIRE_SIGNED_ANDROID === "true" || hasReleaseSigning;

const outputs = {
  debugApk: "android/app/build/outputs/apk/debug/app-debug.apk",
  releaseApkDir: "android/app/build/outputs/apk/release",
  releaseApkMetadata:
    "android/app/build/outputs/apk/release/output-metadata.json",
  releaseBundle: "android/app/build/outputs/bundle/release/app-release.aab",
};

const failures = [];

function requireFile(path, minBytes) {
  if (!existsSync(path)) {
    failures.push(`Missing Android artifact: ${path}`);
    return;
  }

  const size = statSync(path).size;
  if (size < minBytes) {
    failures.push(`Android artifact is unexpectedly small: ${path} (${size} bytes)`);
  }
}

function readJson(path) {
  if (!existsSync(path)) {
    failures.push(`Missing Android metadata: ${path}`);
    return null;
  }

  try {
    return JSON.parse(readFileSync(path, "utf8"));
  } catch (error) {
    failures.push(`Invalid Android metadata JSON at ${path}: ${error.message}`);
    return null;
  }
}

function isZip(path) {
  if (!existsSync(path)) {
    return false;
  }

  return readFileSync(path).subarray(0, 4).equals(Buffer.from("PK\u0003\u0004"));
}

function verifyAabSignature(path) {
  const result = spawnSync("jarsigner", ["-verify", "-strict", path], {
    stdio: "pipe",
    encoding: "utf8",
  });

  if (result.status !== 0) {
    failures.push(
      `Release AAB signature verification failed. Run with signing env configured before Play upload. ${result.stderr.trim()}`,
    );
  }
}

requireFile(outputs.debugApk, 1_000_000);
requireFile(outputs.releaseBundle, 1_000_000);

if (!isZip(outputs.debugApk)) {
  failures.push(`${outputs.debugApk} is not a valid ZIP/APK file.`);
}

if (!isZip(outputs.releaseBundle)) {
  failures.push(`${outputs.releaseBundle} is not a valid ZIP/AAB file.`);
}

const releaseMetadata = readJson(outputs.releaseApkMetadata);
const releaseElement = releaseMetadata?.elements?.[0];
const releaseApkName = releaseElement?.outputFile;
const releaseApkPath = releaseApkName
  ? join(outputs.releaseApkDir, releaseApkName)
  : "";

if (releaseMetadata) {
  if (releaseMetadata.applicationId !== appId) {
    failures.push(
      `Release APK metadata applicationId must be ${appId}; found ${releaseMetadata.applicationId}.`,
    );
  }

  if (releaseMetadata.variantName !== "release") {
    failures.push(
      `Release APK metadata variantName must be release; found ${releaseMetadata.variantName}.`,
    );
  }
}

if (!releaseElement) {
  failures.push("Release APK metadata does not include an output element.");
} else {
  if (releaseElement.versionCode !== versionCode) {
    failures.push(
      `Release APK versionCode must be ${versionCode}; found ${releaseElement.versionCode}.`,
    );
  }

  if (releaseElement.versionName !== versionName) {
    failures.push(
      `Release APK versionName must be ${versionName}; found ${releaseElement.versionName}.`,
    );
  }

  requireFile(releaseApkPath, 1_000_000);

  if (!isZip(releaseApkPath)) {
    failures.push(`${releaseApkPath} is not a valid ZIP/APK file.`);
  }

  if (requireSigned && releaseApkName.includes("unsigned")) {
    failures.push(
      "Release APK is unsigned even though Android release signing is required.",
    );
  }
}

if (requireSigned) {
  verifyAabSignature(outputs.releaseBundle);
}

if (failures.length > 0) {
  console.error("Android artifact audit failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

const signingMode = requireSigned ? "signed release" : "local unsigned release";
console.log(`Android artifact audit passed for ${signingMode} artifacts.`);
