import { request } from "node:https";

const baseUrl = process.env.PENNYRUSH_PRODUCTION_WEB_URL ?? "https://pennyrush.dev";
const requiredPaths = ["/", "/privacy", "/terms", "/robots.txt", "/sitemap.xml"];
const requiredHeaders = [
  "strict-transport-security",
  "x-content-type-options",
  "x-frame-options",
  "referrer-policy",
  "permissions-policy",
];

function fetchHead(url) {
  return new Promise((resolve, reject) => {
    const req = request(url, { method: "HEAD", timeout: 15_000 }, (res) => {
      res.resume();
      resolve({
        statusCode: res.statusCode ?? 0,
        headers: res.headers,
      });
    });

    req.on("timeout", () => {
      req.destroy(new Error(`Timed out while checking ${url}`));
    });
    req.on("error", reject);
    req.end();
  });
}

function isOk(statusCode) {
  return statusCode >= 200 && statusCode < 400;
}

const failures = [];
const parsedBase = new URL(baseUrl);

if (parsedBase.protocol !== "https:") {
  failures.push("PENNYRUSH_PRODUCTION_WEB_URL must use HTTPS.");
}

for (const path of requiredPaths) {
  const url = new URL(path, parsedBase).toString();
  try {
    const result = await fetchHead(url);
    if (!isOk(result.statusCode)) {
      failures.push(`${url} returned HTTP ${result.statusCode}.`);
    }

    if (path === "/") {
      for (const header of requiredHeaders) {
        if (!result.headers[header]) {
          failures.push(`${url} is missing required header: ${header}`);
        }
      }
    }
  } catch (error) {
    failures.push(`${url} failed: ${error.message}`);
  }
}

if (failures.length > 0) {
  console.error("Production web check failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log(`Production web check passed for ${parsedBase.origin}.`);
