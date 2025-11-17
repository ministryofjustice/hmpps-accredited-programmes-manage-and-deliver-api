-- Scheduled --> Awaiting assessment
INSERT INTO referral_status_transition(id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('019a7da6-9a53-7ba9-bf5b-7eb7a1f130fa',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person’s suitability or readiness has changed. The programme team will reassess them.',
        now(),
        null,
        null);

-- Scheduled --> Awaiting allocation
INSERT INTO referral_status_transition(id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('019a7da9-55be-7bab-91bf-48e48585e514',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person cannot start the programme now but may be able to join another group soon.',
        now(),
        null,
        null);

-- On programme --> Awaiting assessment
INSERT INTO referral_status_transition(id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('019a7daf-a68e-77a5-b12c-08f82b02b6b8',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person’s suitability or readiness has changed. The programme team will reassess them.',
        now(),
        null,
        null);


-- On programme --> Awaiting allocation
INSERT INTO referral_status_transition(id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('019a7daf-defa-74ee-9e4c-1378881e658e',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person cannot continue with the group but may be able to join another group in the next 12 weeks (bus stopping).',
        now(),
        null,
        null);