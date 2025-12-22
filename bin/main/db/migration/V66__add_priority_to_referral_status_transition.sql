
-- This column indicates the ordering of the status transitions
ALTER TABLE referral_status_transition
    ADD COLUMN priority SMALLINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT check_priority_positive CHECK (priority > 0);

-- On programme FROM

-- On programme --> Awaiting assessment
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- On programme --> Awaiting allocation
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

-- On programme --> Breach (non-attendance)
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)');

-- On programme --> Recall
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- On programme --> Return to court
UPDATE referral_status_transition
SET priority = 5
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');