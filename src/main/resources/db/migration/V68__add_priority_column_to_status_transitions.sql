
-- This column indicates the ordering of the status transitions
ALTER TABLE referral_status_transition
    ADD COLUMN priority SMALLINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT check_priority_positive CHECK (priority > 0);

-- On programme FROM

-- On programme --> Programme complete(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete');

-- On programme --> Awaiting assessment
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- On programme --> Awaiting allocation
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

-- On programme --> Breach (non-attendance)
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)');

-- On programme --> Recall
UPDATE referral_status_transition
SET priority = 5
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- On programme --> Return to court
UPDATE referral_status_transition
SET priority = 6
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');

-- Scheduled FROM

-- Scheduled --> On programme(isContinuing)
UPDATE referral_status_transition
SET priority = 6
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme');
-- v63 removed this transition then V64 reintroduced this transition

-- Scheduled --> Awaiting assessment
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Scheduled --> Awaiting allocation
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

-- Scheduled --> Deprioritised
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised');

-- Scheduled --> Recall
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- Scheduled --> Return to court
UPDATE referral_status_transition
SET priority = 5
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');

-- Awaiting assessment FROM

-- Awaiting assessment --> Awaiting allocation(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

-- Awaiting assessment --> Suitable but not ready
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready');

-- Awaiting assessment --> Deprioritised
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised');

-- Awaiting assessment --> Recall
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- Awaiting assessment --> Return to court
UPDATE referral_status_transition
SET priority = 5
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');

-- Awaiting allocation FROM

-- Awaiting allocation --> Scheduled(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled');
-- V61 removed this transition, then V64 reintroduced this transition

-- Awaiting allocation --> Deprioritised
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised');

-- Awaiting allocation --> Recall
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- Awaiting allocation --> Return to court
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');

-- Return to Court FROM

-- Return to Court --> Deferred
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deferred');

-- Return to Court --> Withdrawn
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn');

-- Suitable but not ready FROM

-- Suitable but not ready --> Awaiting assessment(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Suitable but not ready --> Deprioritised
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised');

-- Suitable but not ready --> Recall
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- Suitable but not ready --> Return to court
UPDATE referral_status_transition
SET priority = 4
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');


-- Deprioritised FROM

-- Deprioritised --> Awaiting assessment(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Deprioritised --> Recall
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

-- Deprioritised --> Return to court
UPDATE referral_status_transition
SET priority = 3
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');


-- Breach (non-attendance) FROM

-- Breach (non-attendance) --> Awaiting assessment(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Breach (non-attendance) --> Withdrawn
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn');

-- Recall FROM

-- Recall --> Awaiting assessment(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Recall --> Withdrawn
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn');

-- Deferred FROM

-- Deferred --> Awaiting assessment(isContinuing)
UPDATE referral_status_transition
SET priority = 1
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deferred')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

-- Deferred --> Return to court
UPDATE referral_status_transition
SET priority = 2
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deferred')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');
