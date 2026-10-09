ALTER TABLE referral
    ADD COLUMN person_forename TEXT NULL,
    ADD COLUMN person_middle_names TEXT NULL,
    ADD COLUMN person_surname TEXT NULL;

ALTER TABLE facilitator
    ADD COLUMN person_forename TEXT NULL,
    ADD COLUMN person_middle_names TEXT NULL,
    ADD COLUMN person_surname TEXT NULL;
