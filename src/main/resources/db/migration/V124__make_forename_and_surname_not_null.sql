ALTER TABLE referral
    ALTER COLUMN person_forename SET DEFAULT 'UNKNOWN';
ALTER TABLE referral
    ALTER COLUMN person_surname SET DEFAULT 'UNKNOWN';

UPDATE referral
SET person_forename = 'UNKNOWN'
WHERE person_forename IS NULL;

UPDATE referral
SET person_surname = 'UNKNOWN'
WHERE person_surname IS NULL;

ALTER TABLE referral
    ALTER COLUMN person_forename SET NOT NULL;
ALTER TABLE referral
    ALTER COLUMN person_surname SET NOT NULL;

ALTER TABLE facilitator
    ALTER COLUMN person_forename SET DEFAULT 'UNKNOWN';
ALTER TABLE facilitator
    ALTER COLUMN person_surname SET DEFAULT 'UNKNOWN';

UPDATE facilitator
SET person_forename = 'UNKNOWN'
WHERE person_forename IS NULL;

UPDATE facilitator
SET person_surname = 'UNKNOWN'
WHERE person_surname IS NULL;

ALTER TABLE facilitator
    ALTER COLUMN person_forename SET NOT NULL;
ALTER TABLE facilitator
    ALTER COLUMN person_surname SET NOT NULL;


