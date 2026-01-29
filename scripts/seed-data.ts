#!/usr/bin/env bun
// To install bun: https://bun.com/

/**
 * Seed data script for local development
 * This script can be called from any repo that needs seeded data
 *
 * Usage: bun run seed-data.ts [command] [options]
 */

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";
const DEFAULT_COUNT = 50;

console.log({ API_BASE_URL, DEFAULT_COUNT })

function usage(): void {
  console.log(`Usage: bun run seed-data.ts [command] [options]`);
  console.log("");
  console.log("Commands:");
  console.log(`  seed [count]    Create seeded referrals (default: ${DEFAULT_COUNT})`);
  console.log("  teardown        Remove all seeded data");
  console.log("  health          Check if seeding endpoints are available");
  console.log("");
  console.log("Examples:");
  console.log("  bun run seed-data.ts seed 100     # Create 100 referrals");
  console.log("  bun run seed-data.ts teardown     # Remove all seeded data");
}

function parseArgs(args: string[]): { command: string | undefined; count: number } {
  // args[0] is bun, args[1] is the script path, rest are actual arguments
  const scriptArgs = args.slice(2);
  const command = scriptArgs[0];
  const countArg = scriptArgs[1];
  const count = countArg ? parseInt(countArg, 10) : DEFAULT_COUNT;

  return { command, count: isNaN(count) ? DEFAULT_COUNT : count };
}

async function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function fetchWithTimeout(url: string, options?: RequestInit): Promise<Response | null> {
    const baseOptions = {
        'Content-Type': 'application/json',
    };
    options = {
        ...baseOptions,
        ...options,
    }
  try {
    return await fetch(url, options);
  } catch {
    return null;
  }
}

function formatJson(text: string): string {
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}

async function seed(count: number): Promise<void> {
  console.log(`Seeding ${count} referrals...`);

  const response = await fetchWithTimeout(`${API_BASE_URL}/dev/seed/referrals?count=${count}`, {
    method: "POST",
  });

  if (response) {
    const text = await response.text();
    console.log(formatJson(text));
  } else {
    console.error("Failed to seed referrals");
  }

  console.log("");
  console.log("Seeding complete. You may need to restart Wiremock to pick up new stubs.");
}

async function teardown(): Promise<void> {
  console.log("Removing all seeded data...");

  const response = await fetchWithTimeout(`${API_BASE_URL}/dev/seed/referrals`, {
    method: "DELETE",
  });

  if (response) {
    const text = await response.text();
    console.log(formatJson(text));
  } else {
    console.error("Failed to teardown seeded data");
  }
}

async function health(): Promise<void> {
  console.log("Checking seeding endpoint health...");

  const response = await fetchWithTimeout(`${API_BASE_URL}/dev/seed/health`);

  if (response && response.ok) {
    const text = await response.text();
    console.log(formatJson(text));
    console.log("Seeding endpoints are available");
  } else {
    console.error("Seeding endpoints are NOT available");
    console.error("Ensure the API is running with the 'seeding' profile active");
    process.exit(1);
  }
}

async function main(): Promise<void> {
  const { command, count } = parseArgs(process.argv);

  switch (command) {
    case "seed":
      await seed(count);
      break;
    case "teardown":
      await teardown();
      break;
    case "health":
      await health();
      break;
    default:
      usage();
      process.exit(1);
  }
}

main();

