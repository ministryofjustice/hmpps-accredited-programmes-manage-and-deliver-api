-- Insert the new status transitions
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('a7bf75aa-854a-44a9-80a0-aaa60db73a11',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The programme team will assess or reassess the person’s suitability and readiness.',
        now(),
        null,
        null,
        false,
        1);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('fd183ce3-b70d-404c-ae3e-1be1fecb0b6a',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('d5cafc5c-71b8-406a-8cb0-b0dbb3025217',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('ebb506b6-b509-4e0b-ac7c-31671f178c83',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('afaac4ea-2706-4431-aca9-0cfca39b4128',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('c45541aa-b565-4d61-aa22-ec925d10e557',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('f0983e18-3555-43dc-9b27-e405789dc83b',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('50527256-938f-4b14-ae10-3d4b87359476',
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('53d3f549-a104-4751-b8d0-d7d18b008933',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('22b601d6-44cf-4a48-958d-8fc6d30abd42',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('de70354c-9d90-4076-9224-a76d976c44bf',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled to prison.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('a05a6374-8153-41be-98ab-89b14781b34c',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral should be returned to court.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('ceb0ea09-178c-4e52-b65f-a2d2ab6e93fe',
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('eb3603e1-1368-4dbd-80f3-e57c586ca13b',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('bbddca65-b5a7-4bfa-9a8d-0a3db5e158bb',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('cfcd9ec4-5059-4157-8ed5-277a99afc116',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled to prison.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('ab45cb0b-446e-4b41-8243-12cca932846d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('dac79a0a-4f48-4adf-938b-b3ced2c8d931',
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('d3c4bf17-c6fd-45c9-8891-4d0c33c66d76',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('b82065fa-7f80-4d1a-ae8a-2f2f69889647',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('2258dc7a-dfc8-4ede-987a-ea1e172fb7f3',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        8);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('f190c9cd-ace4-4ab9-ac54-a527b1f99fb4',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('d56d4684-33d0-4835-ac36-9c20cb61f4b4',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('6397fe43-2ef7-43a1-8f3f-1c02f4031e95',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('aceb1326-8429-4d5e-83dc-05e3215c2421',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral should be returned to court.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('87155370-492c-452d-b2d3-488015a34426',
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('01792434-8ab6-4ac2-9418-2686dce51b08',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('b5e270a3-240f-4630-8026-dfb1f62fb393',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The programme team will assess or reassess the person’s suitability and readiness.',
        now(),
        null,
        null,
        false,
        1);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('0408730d-318a-49dd-9333-e682dc63f809',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('b918f9e2-bf17-40ac-8a45-890232e1be3a',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled to prison.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('7d6ead37-07d2-41c9-84d3-0b40ca0a998d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('f6503a69-e798-488c-ab32-cac6b3bf8019',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('b454dace-29ed-4238-8b29-d033358d8b2e',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('2a2aa0d3-00fd-4c20-b802-fc65d5bfafcf',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('4e165d83-97d3-414d-806a-b0a75a773eed',
        (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        8);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('6f872dba-7556-4eb9-ab1b-4087d675d90e',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('cba05d8c-0c79-4da2-a65a-4e37e88533f2',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('43133dc9-65ab-4eaa-9cf2-fd723cf8babe',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('e212e1e7-46c1-4f98-8e1c-fada9452a24e',
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('a6e63d4e-208f-4848-938c-ca90e17888f5',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation'),
        'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.',
        now(),
        null,
        null,
        false,
        2);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('50f37f8d-e8ec-46f0-a769-94db25baa18e',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment'),
        'The programme team will assess or reassess the person’s suitability and readiness.',
        now(),
        null,
        null,
        false,
        1);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('5ca5479e-b5da-452d-81ad-03b7d069d30d',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)'),
        'The person has breached the conditions related to their programme requirement.',
        now(),
        null,
        null,
        false,
        5);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('bea7526c-73ba-4e03-b2cc-4cebf959d6ca',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deferred'),
        'The person still needs to complete the programme, but their referral will be deferred until they can continue.',
        now(),
        null,
        null,
        false,
        7);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('bae3cb7a-b1bd-4b50-a3b8-1b4c5281d5ba',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Recall'),
        'The person has been recalled to prison.',
        now(),
        null,
        null,
        false,
        6);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('984394bd-c791-4ae4-9425-81c08e9f2e44',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Return to court'),
        'The person is not suitable for the programme or cannot continue with it. The referral should be returned to court.',
        now(),
        null,
        null,
        false,
        4);

INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing, priority)
VALUES ('add95308-5363-413f-b41c-9340546f9f50',
        (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready'),
        'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.',
        now(),
        null,
        null,
        false,
        3);

-- Fix priorities for existing transitions and remove deprioritized transitions

-- Awaiting allocation
UPDATE referral_status_transition SET priority = 3
WHERE id = 'f12b7ec5-0077-4724-aa9c-ae2cab4a077b';

UPDATE referral_status_transition SET priority = 5
WHERE id = 'e960ae5f-e4dc-4173-bf63-489721a4b182';

DELETE FROM referral_status_transition
WHERE id = '18700159-18a5-4bb5-8064-de4de0ff40fe';

-- Awaiting assessment
DELETE FROM referral_status_transition
WHERE id = '9ad7a938-c682-4250-a50e-f36640002a1a';

UPDATE referral_status_transition SET priority = 3
WHERE id = 'b856ce68-bba0-4be5-a40f-60f35022347d';

UPDATE referral_status_transition SET priority = 5
WHERE id = '4cf408b7-b9bc-4289-8003-df77e6e8719d';

-- Suitable but not ready
DELETE FROM referral_status_transition
WHERE id = '2a3576f9-9241-4f03-b588-cc533503c1de';

UPDATE referral_status_transition SET priority = 3
WHERE id = '2c95edce-c09b-4505-b74e-e3cb87adb585';

UPDATE referral_status_transition SET priority = 5
WHERE id = '77f0e1e4-858a-4bbd-b3fa-960cf16a433d';

-- Scheduled
DELETE FROM referral_status_transition
WHERE id = '496f8ed4-d678-4cdf-8788-b9487d6eab95';

UPDATE referral_status_transition SET priority = 4
WHERE id = '83d37d3a-bfae-42df-aae1-c6386c0f73c7';

UPDATE referral_status_transition SET priority = 6
WHERE id = 'd7ce40e3-c0f6-47d7-879d-bda1cb651277';

-- On programme
UPDATE referral_status_transition SET priority = 5
WHERE id = 'e42c3ce7-167c-4f94-ba4e-92b629b23d8e';

UPDATE referral_status_transition SET priority = 6
WHERE id = '6c18c448-6ed1-4ad9-9738-f0298b14e818';

UPDATE referral_status_transition SET priority = 7
WHERE id = '27ae585c-e8cf-4925-8322-d6304caa4443';

UPDATE referral_status_transition SET priority = 8
WHERE id = 'd3c4bf17-c6fd-45c9-8891-4d0c33c66d76';

UPDATE referral_status_transition SET priority = 9
WHERE id = '2258dc7a-dfc8-4ede-987a-ea1e172fb7f3';

-- Breach (non-attendance)
UPDATE referral_status_transition SET priority = 7
WHERE id = 'df94f7e3-7f84-40ff-abb9-6fc365066d78';

-- Recall
UPDATE referral_status_transition SET priority = 7
WHERE id = '1da86d45-e290-4021-8e78-7af763203f3e';

-- Deferred
UPDATE referral_status_transition SET priority = 4
WHERE id = 'b67377e4-57fa-4917-90b7-0fff8d120c64';

-- Return to court
UPDATE referral_status_transition SET priority = 6
WHERE id = '0b9fda7a-dffd-4782-a0e3-babb1466652c';

UPDATE referral_status_transition SET priority = 7
WHERE id = '9106f24c-ae64-4a1f-90db-9e1be8a8ad3a';

-- Update any existing transition descriptions.
UPDATE referral_status_transition
SET description = 'The programme team will assess or reassess the person''s suitability and readiness.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');

UPDATE referral_status_transition
SET description = 'The person has been assessed as suitable and can be allocated to a group, either for the first time or if they have been bus-stopped.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation');

UPDATE referral_status_transition
SET description = 'The person has been allocated to a scheduled group but has not had their pre-group one-to-one yet.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled');

UPDATE referral_status_transition
SET description = 'The person has started the programme. This means they have had their pre-group one-to-one.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'On programme');

UPDATE referral_status_transition
SET description = 'The person has completed the programme. The referral is closed.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete');

UPDATE referral_status_transition
SET description = 'The person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Suitable but not ready');

UPDATE referral_status_transition
SET description = 'The person is not suitable for the programme or cannot continue with it. The referral should be returned to court.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Return to court');

UPDATE referral_status_transition
SET description = 'The person has breached the conditions related to their programme requirement.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Breach (non-attendance)');

UPDATE referral_status_transition
SET description = 'The person has been recalled to prison.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Recall');

UPDATE referral_status_transition
SET description = 'The referral is paused, for example during a transfer or after the court has extended an order.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Deferred');

UPDATE referral_status_transition
SET description = 'The person cannot complete the programme, for example the court removed the programme requirement or the order expired. The referral is closed.'
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Withdrawn');

-- Update the 'Deferred' description name to now be 'On-hold'
Update referral_status_description
Set description_text = 'On-hold'
Where description_text = 'Deferred';