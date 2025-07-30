insert into referral (id, person_name, crn, created_at, intervention_type, intervention_name, setting)
values ('39fde7e8-d2e3-472b-8364-5848bf673aa6', 'Edgar Schiller', 'X718250', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY'),
       ('54b1a0ac-ab67-4dde-871f-b59b675cbc8c', 'Omar Pfeffer', 'X718252', '2025-07-30 14:51:22.086567', 'ACP',
        'New Me', 'COMMUNITY'),
       ('6885d1f6-5958-40e0-9448-1ff8cc37e643', 'Karen Puckett', 'D002399', '2025-07-30 14:51:22.086567', 'ACP',
        'Horizon', 'COMMUNITY'),
       ('81229aaa-bb3a-4705-8015-99052bafab58', 'Valerie Wyman', 'X718255', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY'),
       ('a8a83c3e-d779-47d5-849f-bb2635666344', 'Sadie Borer', 'X718253', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY'),
       ('aabbadf7-a427-436e-b1a4-f27dd5893c24', 'Mr Joye Hatto', 'D007523', '2025-07-30 14:51:22.086567', 'ACP',
        'Building Choices', 'COMMUNITY');

INSERT INTO referral_status_history(id, status, created_at, created_by, start_date, end_date)
VALUES ('85b59ec2-bd8f-43c4-90f5-3d55b39331b2', 'CREATED', now(), 'USER_ID_12345', now(), null),
       ('c23d4744-2acc-4cec-848d-fd253f942420', 'CREATED', now(), 'USER_ID_12345', now(), null),
       ('1aff155c-9187-4147-ad42-3f4fa399bc16', 'CREATED', now(), 'USER_ID_12345', now(), null),
       ('2cb94a8a-82d1-4b85-bac0-99e92c5b1577', 'CREATED', now(), 'USER_ID_12345', now(), null),
       ('625aaf5d-9913-4e24-8c91-158e9e584b56', 'CREATED', '2025-07-10 00:00:00.000000', 'USER_ID_12345',
        '2025-07-10 00:00:00.000000', '2025-07-10 00:00:00.000000'),
       ('54c7937c-d598-4274-aff9-b40b0e0d80bd', 'STARTED', '2025-07-11 00:00:00.000000', 'USER_ID_12345',
        '2025-07-11 00:00:00.000000', null);

INSERT INTO referral_status_history_mapping(id, referral_id, referral_status_history_id)
VALUES ('1fdd70d2-b03a-4ef8-9aa5-30157cac5bf7', '6885d1f6-5958-40e0-9448-1ff8cc37e643',
        '85b59ec2-bd8f-43c4-90f5-3d55b39331b2'),
       ('2b94e64e-f87a-440c-be77-aafa834573d8', '81229aaa-bb3a-4705-8015-99052bafab58',
        'c23d4744-2acc-4cec-848d-fd253f942420'),
       ('8d5f6674-19fd-467d-adb2-d75c0edf9892', '39fde7e8-d2e3-472b-8364-5848bf673aa6',
        '1aff155c-9187-4147-ad42-3f4fa399bc16'),
       ('a5d05df0-fb68-40b3-8b48-fe496fff9b7e', '54b1a0ac-ab67-4dde-871f-b59b675cbc8c',
        '2cb94a8a-82d1-4b85-bac0-99e92c5b1577'),
       ('1ee3f920-042c-4a0f-894e-fd25b3f383b8', 'a8a83c3e-d779-47d5-849f-bb2635666344',
        '625aaf5d-9913-4e24-8c91-158e9e584b56'),
       ('1b171d93-8d19-4a96-828f-43101070b411', 'a8a83c3e-d779-47d5-849f-bb2635666344',
        '54c7937c-d598-4274-aff9-b40b0e0d80bd');
