-- Awaiting allocation --> Scheduled
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing)
VALUES ('f6ba774b-e2db-4114-8bc5-9cf82f5cde36',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        'The person has been allocated to a scheduled group.',
        now(),
        null,
        null,
        true);

-- Scheduled --> On programme
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing)
VALUES ('071a3a52-02f1-4c71-a6ca-77594a2793ea',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        'The person has started the programme.',
        now(),
        null,
        null,
        true);

-- On programme --> Programme complete
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing)
VALUES ('f3c0b0ec-3b6d-4daa-b863-e447490a36ad',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete'),
        'The person has completed the programme. The referral will be closed.',
        now(),
        null,
        null,
        true);