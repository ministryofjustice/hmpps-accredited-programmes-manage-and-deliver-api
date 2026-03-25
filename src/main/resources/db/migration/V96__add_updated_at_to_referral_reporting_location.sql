ALTER TABLE referral_reporting_location
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN referral_reporting_location.updated_at IS 'referral reporting location last updated time';