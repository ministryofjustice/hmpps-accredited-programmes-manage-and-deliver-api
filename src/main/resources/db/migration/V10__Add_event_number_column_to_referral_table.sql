ALTER TABLE referral
    ADD COLUMN event_id     TEXT NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN event_number TEXT NOT NULL DEFAULT 0;

COMMENT ON COLUMN referral.event_id IS 'This contains the unique identifier for a licence condition or requirement created event from nDelius.';
COMMENT ON COLUMN referral.event_number IS 'This is the number of the event that has been sent from nDelius which starts at 1 and increments for subsequent events.'