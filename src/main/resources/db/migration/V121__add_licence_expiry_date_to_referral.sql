ALTER TABLE referral
    ADD COLUMN licence_expiry_date date NULL;

COMMENT ON COLUMN referral.licence_expiry_date IS 'The expected end date of the licence condition attached to a referral';

CREATE INDEX idx_referral_licence_expiry_date ON referral (licence_expiry_date);
