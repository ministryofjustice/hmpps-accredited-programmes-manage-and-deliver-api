ALTER TABLE referral_reporting_location
    ADD COLUMN region_name TEXT NOT NULL DEFAULT 'UNKNOWN_REGION_NAME';

COMMENT ON COLUMN referral_reporting_location.region_name IS 'The name of the geographic region (aka "area" by nDelius) that the Reporting Location falls under';