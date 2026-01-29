# Seeding System for Local Development

## Overview

For several months, the Manage&Deliver dev team have run into issues which boil down to "I don't have enough data locally to test this".

This seeding system creates semi-realistic data in the database, and creates corresponding Wiremock stubs for nDelius.  This enables all developers, but especially UI developers, to develop with a system that has reliable data (for e.g. pagination, sorting, filtering).

## Quick Start

### 0. Install Bun

Bun is a JavaScript run time that can run .ts files directly (a feature also available in newer versions of Node)

To install bun, see the official webpage: https://bun.com/

### 1. Start the API with the seeding profile

Ensure your application is running with the `seeding` profile active. This can be done by:

**Updating values-dev.yaml**, to make sure the `seeding` profile is present in the `env` block": 

```bash
SPRING_PROFILES_ACTIVE: "dev,seeding"
```

### 2. Generate the data

With the API running, you can either 

**Use the seed-data.sh script:**

```bash
# Create 50 referrals (default)
bun ./scripts/seed-data.ts seed

# Create a specific number of referrals
bun ./scripts/seed-data.ts seed 100

# Remove all seeded data
bun ./scripts/seed-data.ts teardown

# Check if seeding endpoints are available
bun ./scripts/seed-data.ts health
```

**Call the API (e.g. in Postman)**

```bash
# Create 10 referrals
curl -X POST "http://localhost:8080/dev/seed/referrals?count=10"

# Create 100 referrals
curl -X POST "http://localhost:8080/dev/seed/referrals?count=100"

# Remove all seeded data
curl -X DELETE "http://localhost:8080/dev/seed/referrals"

# Health check
curl "http://localhost:8080/dev/seed/health"
```

### 3. Restart Wiremock

Use the Docker UI or:

```bash
# Use docker compose
docker-compose restart wiremock

# Or the container itself
docker container restart hmpps-accredited-programmes-manage-and-deliver-api-wiremock-1
```

## What Gets Created

For each seeded referral, the system creates:

1. **A Referral entity** in the database with:
   - Unique CRN (format: S000001, S000002, etc.)
   - Fake person name, date of birth, sex
   - Status: "Awaiting assessment"
   - Intervention type: ACP (Accredited Programme)
   - Setting: COMMUNITY
   - Cohort: GENERAL_OFFENCE
   - An event ID and number (i.e. relating to the nDelius Requirement)
   - The `is_seeded` field on the Referral is set to `true` to allow easy clean up

2. **A Wiremock stub** in `wiremock_mappings/seeded/` that returns person details when the API calls nDelius

## Security / Best Practices

### Never activate the seeding profile in (pre-) prod.

The seeding system is only active when the `seeding` profile is enabled.  This should **never** be deployed in production-like environments.

### Never commit your generated wiremocks

Each dev will have differently randomly generated seed data.  Conflicting files could cause problems.

The `wiremock_mappings/seeded/` directory is in `.gitignore`
