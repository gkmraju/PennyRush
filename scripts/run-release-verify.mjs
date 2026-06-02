import { spawnSync } from "node:child_process";
import { existsSync } from "node:fs";

const npmCommand = process.platform === "win32" ? "npm.cmd" : "npm";
const gradleCommand = process.platform === "win32" ? "gradlew.bat" : "./gradlew";

const steps = [
  {
    title: "Audit release configuration",
    command: npmCommand,
    args: ["run", "release:audit-config"],
  },
  {
    title: "Audit dependencies",
    command: npmCommand,
    args: ["audit"],
  },
  {
    title: "Run web parser tests",
    command: npmCommand,
    args: ["run", "web:test"],
  },
  {
    title: "Lint web",
    command: npmCommand,
    args: ["run", "web:lint"],
  },
  {
    title: "Typecheck web",
    command: npmCommand,
    args: ["run", "web:typecheck"],
  },
  {
    title: "Build web",
    command: npmCommand,
    args: ["run", "web:build"],
  },
  {
    title: "Compile Android debug Kotlin",
    command: gradleCommand,
    args: [":app:compileDebugKotlin"],
    cwd: "android",
  },
  {
    title: "Test and package Android",
    command: gradleCommand,
    args: [":feature:home:testDebugUnitTest", ":app:assembleDebug", ":app:assembleRelease", ":app:bundleRelease"],
    cwd: "android",
  },
];

if (!existsSync("android/gradlew") && process.platform !== "win32") {
  console.error("Missing Android Gradle wrapper at android/gradlew.");
  process.exit(1);
}

for (const step of steps) {
  console.log(`\n==> ${step.title}`);
  const result = spawnSync(step.command, step.args, {
    cwd: step.cwd,
    env: process.env,
    stdio: "inherit",
    shell: false,
  });

  if (result.status !== 0) {
    console.error(`\nRelease verification failed during: ${step.title}`);
    process.exit(result.status ?? 1);
  }
}

console.log("\nRelease verification passed.");
