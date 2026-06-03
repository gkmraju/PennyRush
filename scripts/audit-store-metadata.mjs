import { existsSync, readFileSync } from "node:fs";

const files = {
  listing: "docs/play-store-listing.md",
  dataSafety: "docs/play-data-safety.md",
  privacy: "docs/privacy-policy.md",
  title: "android/fastlane/metadata/android/en-US/title.txt",
  shortDescription:
    "android/fastlane/metadata/android/en-US/short_description.txt",
  fullDescription: "android/fastlane/metadata/android/en-US/full_description.txt",
  privacyUrl: "android/fastlane/metadata/android/en-US/privacy_url.txt",
  changelog: "android/fastlane/metadata/android/en-US/changelogs/1.txt",
};

const failures = [];

function read(path) {
  if (!existsSync(path)) {
    failures.push(`Missing required store metadata file: ${path}`);
    return "";
  }

  return readFileSync(path, "utf8").trim();
}

function section(markdown, title) {
  const match = markdown.match(
    new RegExp(`(?:^|\\n)## ${title}\\n\\n([\\s\\S]*?)(?=\\n## |$)`),
  );
  return match?.[1]?.trim() ?? "";
}

function requireIncludes(name, body, values) {
  const normalizedBody = body.toLowerCase();
  for (const value of values) {
    if (!normalizedBody.includes(value.toLowerCase())) {
      failures.push(`${name} does not include required text: ${value}`);
    }
  }
}

const listing = read(files.listing);
const dataSafety = read(files.dataSafety);
const privacy = read(files.privacy);
const title = read(files.title);
const shortDescription = read(files.shortDescription);
const fullDescription = read(files.fullDescription);
const privacyUrl = read(files.privacyUrl);
const changelog = read(files.changelog);

const listingShortDescription = section(listing, "Short Description");
const listingFullDescription = section(listing, "Full Description");

if (title !== "PennyRush") {
  failures.push("Play title must be exactly PennyRush.");
}

if (shortDescription !== listingShortDescription) {
  failures.push(
    "Fastlane short_description.txt does not match docs/play-store-listing.md.",
  );
}

if (fullDescription !== listingFullDescription) {
  failures.push(
    "Fastlane full_description.txt does not match docs/play-store-listing.md.",
  );
}

if (shortDescription.length === 0 || shortDescription.length > 80) {
  failures.push(
    `Play short description must be 1-80 characters; found ${shortDescription.length}.`,
  );
}

if (fullDescription.length === 0 || fullDescription.length > 4000) {
  failures.push(
    `Play full description must be 1-4000 characters; found ${fullDescription.length}.`,
  );
}

if (privacyUrl !== "https://pennyrush.dev/privacy") {
  failures.push("Play privacy URL must be https://pennyrush.dev/privacy.");
}

requireIncludes("Play full description", fullDescription, [
  "CSV statement import",
  "Receipt scan",
  "Budget and goal tracking",
  "Activity export as CSV",
  "no ads and no tracking SDKs",
]);

requireIncludes("Play changelog", changelog, [
  "CSV import",
  "receipt scan",
  "budgets",
  "goals",
  "privacy controls",
]);

requireIncludes("Play Data Safety", dataSafety, [
  "Ads: No",
  "Third-party tracking SDKs: No",
  "Account deletion: Yes",
  "Raw CSV statement files are not uploaded",
  "receipt images are not stored by PennyRush after scan review",
]);

requireIncludes("Privacy policy", privacy, [
  "PennyRush does not sell personal data",
  "Raw CSV statement files are not uploaded",
  "receipt images are not stored by PennyRush after scan review",
  "delete your account",
]);

if (failures.length > 0) {
  console.error("Store metadata audit failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log("Store metadata audit passed.");
