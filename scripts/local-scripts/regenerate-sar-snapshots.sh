#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
RESOURCES_DIR="$PROJECT_DIR/src/test/resources"
SAR_DIR="$RESOURCES_DIR/sar"

BASE_TEST="uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.sar.SarContractIntegrationTest"

echo "==> Regenerating SAR snapshots..."
echo ""

SAR_GENERATE_ACTUAL=true "$PROJECT_DIR/gradlew" -p "$PROJECT_DIR" test \
  --tests "$BASE_TEST.JPA generated entity schema should match expected snapshot" \
  --tests "$BASE_TEST.SAR API should return expected data" \
  --tests "$BASE_TEST.SAR report should render as expected" || true

echo ""
echo "==> Copying generated snapshot files to $SAR_DIR..."

LOG_FILES=(
  "$RESOURCES_DIR/entity-schema.json.log"
  "$RESOURCES_DIR/sar-api-response.json.log"
  "$RESOURCES_DIR/sar-generated-report.html.log"
)

SNAPSHOT_FILES=(
  "$SAR_DIR/entity-schema-snapshot.json"
  "$SAR_DIR/sar-api-response.json"
  "$SAR_DIR/sar-expected-render-result.html"
)

for i in "${!LOG_FILES[@]}"; do
  log_file="${LOG_FILES[$i]}"
  snapshot_file="${SNAPSHOT_FILES[$i]}"
  if [ -f "$log_file" ]; then
    cp "$log_file" "$snapshot_file"
    echo "  Copied $(basename "$log_file") -> sar/$(basename "$snapshot_file")"
    rm "$log_file"
    echo "  Deleted $log_file"
  else
    echo "  WARNING: $log_file not found — skipping"
  fi
done

echo ""
echo "==> Done. Run ./gradlew test --tests \"*SarContractIntegrationTest*\" to verify."

