
-- This column indicates whether the transition represents the "happy path" of a referral
-- from creation through to completion (true), or represents a backwards/sideways transition (false)
ALTER TABLE referral_status_transition
ADD COLUMN is_continuing BOOLEAN NOT NULL DEFAULT false;

-- Update transitions that represent the happy path to have is_continuing = true

-- Awaiting assessment --> Awaiting allocation
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

-- Awaiting allocation --> Scheduled
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled');

-- Scheduled --> On programme
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme');

-- On programme --> Programme complete
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete');

-- Suitable but not ready --> Awaiting assessment
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Deprioritised --> Awaiting assessment
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Breach (non-attendance) --> Awaiting assessment
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Recall --> Awaiting assessment
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Deferred --> Awaiting assessment
UPDATE referral_status_transition
SET is_continuing = true
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deferred')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');
