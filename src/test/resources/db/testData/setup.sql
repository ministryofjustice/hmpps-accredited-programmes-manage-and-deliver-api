INSERT INTO referral_status_description
(id, description_text,
 is_closed,
 label_colour,
 created_at,
 updated_at, deleted_at)
VALUES ('76b2f8d8-260c-4766-a716-de9325292609', 'Awaiting Assessment', false, null, now(), now(), null);

insert into referral (id, person_name, crn, created_at, intervention_type, intervention_name, setting, event_id,
                      event_number, cohort)
values ('39fde7e8-d2e3-472b-8364-5848bf673aa6', 'Edgar Schiller', 'X718250', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY', '2500828798', 1, 'SEXUAL_OFFENCE'),
       ('54b1a0ac-ab67-4dde-871f-b59b675cbc8c', 'Omar Pfeffer', 'X718252', '2025-07-30 14:51:22.086567', 'ACP',
        'New Me', 'COMMUNITY', '2500828799', 1, 'GENERAL_OFFENCE'),
       ('6885d1f6-5958-40e0-9448-1ff8cc37e643', 'Karen Puckett', 'D002399', '2025-07-30 14:51:22.086567', 'ACP',
        'Horizon', 'COMMUNITY', '2500825678', 1, 'GENERAL_OFFENCE'),
       ('81229aaa-bb3a-4705-8015-99052bafab58', 'Valerie Wyman', 'X718255', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY', '3457828798', 1, 'SEXUAL_OFFENCE'),
       ('a8a83c3e-d779-47d5-849f-bb2635666344', 'Sadie Borer', 'X718253', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY', '7619828798', 1, 'GENERAL_OFFENCE'),
       ('aabbadf7-a427-436e-b1a4-f27dd5893c24', 'Mr Joye Hatto', 'D007523', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY', '0987628798', 1, 'GENERAL_OFFENCE');

INSERT INTO referral_status_history(id, referral_id, referral_status_description_id, created_at, created_by, start_date, end_date)
VALUES ('85b59ec2-bd8f-43c4-90f5-3d55b39331b2', '39fde7e8-d2e3-472b-8364-5848bf673aa6', '76b2f8d8-260c-4766-a716-de9325292609', now(), 'USER_ID_12345', now(), null),
       ('c23d4744-2acc-4cec-848d-fd253f942420', '54b1a0ac-ab67-4dde-871f-b59b675cbc8c', '76b2f8d8-260c-4766-a716-de9325292609', now(), 'USER_ID_12345', now(), null),
       ('1aff155c-9187-4147-ad42-3f4fa399bc16', '6885d1f6-5958-40e0-9448-1ff8cc37e643', '76b2f8d8-260c-4766-a716-de9325292609', now(), 'USER_ID_12345', now(), null),
       ('2cb94a8a-82d1-4b85-bac0-99e92c5b1577', '81229aaa-bb3a-4705-8015-99052bafab58', '76b2f8d8-260c-4766-a716-de9325292609', now(), 'USER_ID_12345', now(), null),
       ('625aaf5d-9913-4e24-8c91-158e9e584b56', 'a8a83c3e-d779-47d5-849f-bb2635666344', '76b2f8d8-260c-4766-a716-de9325292609', '2025-07-10 00:00:00.000000', 'USER_ID_12345',
        '2025-07-10 00:00:00.000000', '2025-07-10 00:00:00.000000'),
       ('54c7937c-d598-4274-aff9-b40b0e0d80bd', 'aabbadf7-a427-436e-b1a4-f27dd5893c24', '76b2f8d8-260c-4766-a716-de9325292609', '2025-07-11 00:00:00.000000', 'USER_ID_12345',
        '2025-07-11 00:00:00.000000', null);

