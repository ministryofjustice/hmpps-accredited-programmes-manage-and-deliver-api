#!/bin/bash

# Seed data script for local development
# This script can be called from any repo that needs seeded data

set -e

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
DEFAULT_COUNT=50

usage() {
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  seed [count]    Create seeded referrals (default: $DEFAULT_COUNT)"
    echo "  teardown        Remove all seeded data"
    echo "  health          Check if seeding endpoints are available"
    echo ""
    echo "Examples:"
    echo "  $0 seed 100     # Create 100 referrals"
    echo "  $0 teardown     # Remove all seeded data"
}

wait_for_api() {
    echo "Waiting for API to be ready..."
    for i in {1..30}; do
        if curl -s "${API_BASE_URL}/dev/seed/health" > /dev/null 2>&1; then
            echo "API is ready"
            return 0
        fi
        sleep 1
    done
    echo "ERROR: API did not become ready in time"
    echo "Ensure the API is running with the 'seeding' profile active"
    exit 1
}

seed() {
    local count="${1:-$DEFAULT_COUNT}"
    wait_for_api

    echo "Seeding $count referrals..."
    response=$(curl -s -X POST "${API_BASE_URL}/dev/seed/referrals?count=${count}")
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
    echo "Seeding complete. You may need to restart Wiremock to pick up new stubs."
}

teardown() {
    wait_for_api

    echo "Removing all seeded data..."
    response=$(curl -s -X DELETE "${API_BASE_URL}/dev/seed/referrals")
    echo "$response" | jq . 2>/dev/null || echo "$response"
}

health() {
    echo "Checking seeding endpoint health..."
    if curl -s "${API_BASE_URL}/dev/seed/health" | jq . 2>/dev/null; then
        echo "Seeding endpoints are available"
    else
        echo "Seeding endpoints are NOT available"
        echo "Ensure the API is running with the 'seeding' profile active"
        exit 1
    fi
}

case "${1:-}" in
    seed)
        seed "$2"
        ;;
    teardown)
        teardown
        ;;
    health)
        health
        ;;
    *)
        usage
        exit 1
        ;;
esac

