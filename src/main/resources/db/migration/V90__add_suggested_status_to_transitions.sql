ALTER TABLE referral_status_transition
    ADD COLUMN is_suggested BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN referral_status_transition.is_suggested IS 'Should this status be suggested on the UI as part of the transition';

-- Add a unique constraint so that there is only one 'is_suggested' status for each from status.
CREATE UNIQUE INDEX uq_referral_status_transition_suggested_from
    ON referral_status_transition (from_status)
    WHERE is_suggested = TRUE;

UPDATE referral_status_transition
SET is_suggested = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme');

UPDATE referral_status_transition
SET is_suggested = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete');
