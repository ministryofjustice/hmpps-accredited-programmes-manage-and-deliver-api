# Seeding System for Local Development

## Overview

For several months, the Manage&Deliver dev team have run into issues which boil down to "I don't have enough data locally to test this".

This seeding system creates semi-realistic data in the database, and creates corresponding Wiremock stubs for nDelius.  This enables all developers, but especially UI developers, to develop with a system that has reliable data (for e.g. pagination, sorting, filtering).

## Quick Start

### 1. Start the API with the seeding profile

Ensure your application is running with the `seeding` profile active. This can be done by:

**Updating Run Configurations**, to make sure the `seeding` profile is present in the `env` block" in your "Run Configuration" in IntelliJ.  This should already be commited in the `.xml.run` files in this repo.

### 2. Generate the data

With the API running, you can either 

**Use the seed-data.sh script:**

```bash
# Create 50 referrals (default)
sh ./scripts/seed-data.sh seed

# Create a specific number of referrals
sh ./scripts/seed-data.sh seed 100

# DANGER: Delete ALL referrals from the database
sh ./scripts/seed-data.sh teardown

# Check if seeding endpoints are available
sh ./scripts/seed-data.sh health
```

**Call the API (e.g. in Postman)**

```bash
# Create 10 referrals
curl -X POST "http://localhost:8080/dev/seed/referrals?count=10"

# Create 100 referrals
curl -X POST "http://localhost:8080/dev/seed/referrals?count=100"

# DANGER: Delete ALL referrals from the database
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

2. **A Wiremock stub** in `wiremock_mappings/seeded/` that returns person details when the API calls nDelius

## Security / Best Practices

### Never activate the seeding profile in (pre-) prod.

The seeding system is only active when the `seeding` profile is enabled.  This should **never** be deployed in production-like environments.

**⚠️ DANGER: The DELETE endpoint (`/dev/seed/referrals`) will delete ALL referrals from the database, not just seeded ones.** This is intentionally destructive for local development convenience, but would be catastrophic in a production environment.

### Never commit your generated wiremocks

Each dev will have differently randomly generated seed data.  Conflicting files could cause problems.

The `wiremock_mappings/seeded/` directory is in `.gitignore`
