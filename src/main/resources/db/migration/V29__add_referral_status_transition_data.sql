-- Awaiting assessment --> Awaiting allocation
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('550e8400-e29b-41d4-a716-446655440020',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group.',
        now(),
        null,
        null);
-- Awaiting assessment --> Suitable but not ready
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('1a8e2fd8-8622-4d16-a783-823891c7799f',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null);

-- Awaiting assessment --> Deprioritised
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('9ad7a938-c682-4250-a50e-f36640002a1a',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        'The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are re-prioritised.',
        now(),
        null,
        null);
-- Awaiting assessment --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('4cf408b7-b9bc-4289-8003-df77e6e8719d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- Awaiting assessment --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('b856ce68-bba0-4be5-a40f-60f35022347d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);

-- Awaiting allocation --> Scheduled
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('f6ba774b-e2db-4114-8bc5-9cf82f5cde36',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        'The person has been allocated to a scheduled group.',
        now(),
        null,
        null);
-- Awaiting allocation --> Deprioritised
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('18700159-18a5-4bb5-8064-de4de0ff40fe',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        'The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are reprioritised.',
        now(),
        null,
        null);
-- Awaiting allocation --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('e960ae5f-e4dc-4173-bf63-489721a4b182',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- Awaiting allocation --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('f12b7ec5-0077-4724-aa9c-ae2cab4a077b',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);

-- Scheduled --> On programme
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('071a3a52-02f1-4c71-a6ca-77594a2793ea',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        'The person has started the programme.',
        now(),
        null,
        null);
-- Scheduled --> Deprioritised
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('496f8ed4-d678-4cdf-8788-b9487d6eab95',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        'The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are reprioritised.',
        now(),
        null,
        null);
-- Scheduled --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('d7ce40e3-c0f6-47d7-879d-bda1cb651277',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- Scheduled --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('83d37d3a-bfae-42df-aae1-c6386c0f73c7',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);


-- On programme --> Programme complete
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('f3c0b0ec-3b6d-4daa-b863-e447490a36ad',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete'),
        'The person has completed the programme. The referral will be closed.',
        now(),
        null,
        null);
-- On programme --> Breach (non-attendance)
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('6c18c448-6ed1-4ad9-9738-f0298b14e818',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached their conditions through non-attendance of an Accredited Programme. The referral will stay open while this is investigated.',
        now(),
        null,
        null);
-- On programme --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('27ae585c-e8cf-4925-8322-d6304caa4443',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- On programme --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('e42c3ce7-167c-4f94-ba4e-92b629b23d8e',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);


-- Return to court --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('0b9fda7a-dffd-4782-a0e3-babb1466652c',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null);
-- Return to court --> Withdrawn
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('9106f24c-ae64-4a1f-90db-9e1be8a8ad3a',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The programme requirement has been removed by the court. The referral will be closed.',
        now(),
        null,
        null);


-- Suitable but not ready --> Awaiting assessment
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('a329c768-cb73-4860-b78f-ec3030de60b0',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person’s suitability or readiness has changed. The programme team will reassess them.',
        now(),
        null,
        null);
-- Suitable but not ready --> Deprioritised
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('2a3576f9-9241-4f03-b588-cc533503c1de',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        'The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are reprioritised.',
        now(),
        null,
        null);
-- Suitable but not ready --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('77f0e1e4-858a-4bbd-b3fa-960cf16a433d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- Suitable but not ready --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('2c95edce-c09b-4505-b74e-e3cb87adb585',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);


-- Deprioritised --> Awaiting assessment
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('580041aa-141c-4dc6-8993-88b30b10cb85',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person has been reprioritised. The programme team will assess the person’s suitability and readiness.',
        now(),
        null,
        null);
-- Deprioritised --> Recall
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('f96fc179-8ce3-4871-a105-e87eee1b82b4',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.',
        now(),
        null,
        null);
-- Deprioritised --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('7cfd59d8-af26-464d-a07c-efdd4ea631e3',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);

-- Breach (non-attendance) --> Awaiting assessment
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('ba80931e-8fa5-42ce-a4be-7ee5c83530d1',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The breach has been investigated and the person’s referral can continue. The programme team will assess the person’s suitability and readiness.',
        now(),
        null,
        null);
-- Breach (non-attendance) --> Withdrawn
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('df94f7e3-7f84-40ff-abb9-6fc365066d78',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example because they have been recalled. The referral will be closed.',
        now(),
        null,
        null);


-- Recall --> Awaiting assessment
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('0355327a-f7a1-4656-8d67-1d92c18a2034',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person’s referral can continue, for example after a short-term recall. The programme team will assess the person’s suitability and readiness.',
        now(),
        null,
        null);
-- Recall --> Withdrawn
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('1da86d45-e290-4021-8e78-7af763203f3e',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example because they have been recalled. The referral will be closed.',
        now(),
        null,
        null);


-- Deferred --> Awaiting assessment
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('03d5094a-6d4c-4cc0-9a48-f9bdc8680cba',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The person’s referral can now continue. The programme team will assess the person’s suitability and readiness.',
        now(),
        null,
        null);
-- Deferred --> Return to court
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at)
VALUES ('b67377e4-57fa-4917-90b7-0fff8d120c64',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.',
        now(),
        null,
        null);