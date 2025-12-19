ALTER TABLE referral_status_transition
    ADD COLUMN visible BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN referral_status_transition.visible IS 'Should this column be visible in the status transitions view';

-- Set not visible on transition from 'Scheduled' to 'On Programme'
UPDATE referral_status_transition
SET visible = FALSE
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme');

-- Set not visible on transition from 'On Programme' to 'Programme Complete'
UPDATE referral_status_transition
SET visible = FALSE
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete');