DELETE
FROM referral;

ALTER TABLE referral
    ADD COLUMN event_number TEXT NOT NULL DEFAULT 'UNKNOWN';

COMMENT ON COLUMN referral.event_number IS 'This contains the unique identifier for a licence condition or requirement created event from nDelius.'