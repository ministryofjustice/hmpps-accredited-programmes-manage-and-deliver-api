-- Getting Started 1-1
UPDATE module_session_template SET module_id = '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', session_number = 3 WHERE module_id = '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d';
UPDATE module SET module_number = '2' WHERE id = '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d';
DELETE FROM module WHERE id in ('23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d');

-- Managing Myself 1-1
UPDATE module_session_template SET module_id = '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', session_number = 7 WHERE module_id = '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f';
UPDATE module SET module_number = '3' WHERE id in ('34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f');
DELETE FROM module WHERE id in ('45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f');

-- Managing Life’s Problems 1-1
UPDATE module_session_template SET module_id = '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', session_number = 5 WHERE module_id = '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b';
UPDATE module SET module_number = '4' WHERE id in ('56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b');
DELETE FROM module WHERE id in ('67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b');

-- Managing People Around Me 1-1
UPDATE module_session_template SET module_id = '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', session_number = 7 WHERE module_id = '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d';
UPDATE module SET module_number = '5' WHERE id in ('78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d');
DELETE FROM module WHERE id in ('89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d');

-- Bringing it all together (update module number)
UPDATE module SET module_number = '6' WHERE id = '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e';

-- new module and associated session for Post-programme reviews
INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid 'ac581f6c-1d81-45a2-af1e-7e3a041ae756', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Post-programme reviews', 7);

INSERT INTO module_session_template (id, module_id, session_number, session_type, pathway, name, duration_minutes)
Values(uuid 'fb60da35-7de1-4d25-a03d-6ff2c9baec3f', uuid 'ac581f6c-1d81-45a2-af1e-7e3a041ae756', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Post-programme reviews', 60);
