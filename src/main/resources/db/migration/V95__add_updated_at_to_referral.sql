ALTER TABLE referral
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN referral.updated_at IS 'referral updated time';