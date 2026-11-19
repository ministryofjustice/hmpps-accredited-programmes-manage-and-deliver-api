ALTER TABLE referral
    ADD COLUMN actual_start_date TIMESTAMP NULL;

COMMENT ON COLUMN referral.actual_start_date IS 'The date and time of the first pre-group one-to-one session that was attended. Set once and never overwritten by subsequent pre-group one-to-ones.';
