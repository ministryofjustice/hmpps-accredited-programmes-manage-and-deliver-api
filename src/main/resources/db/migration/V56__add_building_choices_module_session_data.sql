-- Seed data for Accredited Programme Template: "Building Choices"
-- Populates accredited_programme_template, module, and module_session_template

-- Create the accredited_programme_template
INSERT INTO accredited_programme_template (id, name, valid_from, valid_until)
VALUES (uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Building Choices', CURRENT_DATE, NULL);

-- Insert modules individually (explicit ids, keep module_number ordering)
INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Pre-Group', 1);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Started', 2);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Started 1-1', 3);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Myself', 4);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Myself 1-1', 5);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Life’s Problems', 6);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Life’s Problems 1-1', 7);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing People Around Me', 8);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing People Around Me 1-1', 9);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Bringing It All Together', 10);

-- Insert session templates (both pathways) for each module
INSERT INTO module_session_template (id, module_id, session_number, session_type, pathway, name, duration_minutes)

-- 1) Pre-Group (Individual); Moderate = 1
SELECT uuid '1bcaf371-e624-4034-a13b-5ae2e9921bd4', uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Pre-Group', 60

-- 2) Getting Started (Group); Moderate = 2
UNION ALL SELECT uuid 'e17396c4-0638-4100-7f90-e7f8091a2b3c', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 1, 'GROUP', 'MODERATE_INTENSITY', 'Introduction to Building Choices', 150
UNION ALL SELECT uuid 'f284a7d5-1749-4211-80a1-f8091a2b3c4d', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 2, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Myself', 150

-- 3) Getting Started 1-1 (Individual); Moderate = 1
UNION ALL SELECT uuid 'c5b7da08-4a7c-4544-b3d4-2b3c4d5e6f70', uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Getting Started 1-1', 60

-- 4) Managing Myself (Group); Moderate = 6
UNION ALL SELECT uuid 'e7d9fc2a-6c9e-4766-d5f6-4d5e6f708192', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding my feelings', 150
UNION ALL SELECT uuid 'f8ea0d3b-7daf-4877-e607-5e6f708192a3', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 2, 'GROUP', 'MODERATE_INTENSITY', 'Helpful/Unhelpful Feelings', 150
UNION ALL SELECT uuid '09fb1e4c-8eb0-4988-f718-6f708192a3b4', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 3, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 1', 150
UNION ALL SELECT uuid '1a0c2f5d-9fc1-4a99-0829-70708192a3b4', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 4, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 2', 150
UNION ALL SELECT uuid '2b1d306e-a1d2-4baa-193a-808192a3b4c5', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 5, 'GROUP', 'MODERATE_INTENSITY', 'Understanding My Thinking', 150
UNION ALL SELECT uuid '3c2e417f-b2e3-4bbb-2a4b-90192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 6, 'GROUP', 'MODERATE_INTENSITY', 'Developing My Flexible Thinking', 150

-- 5) Managing Myself 1-1 (Individual); Moderate = 1
UNION ALL SELECT uuid 'd6b8eb19-5b8d-4555-c4e5-30192a3b4c5d', uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Myself 1-1', 60

-- 6) Managing Life’s Problems (Group); Moderate = 4
UNION ALL SELECT uuid 'f8da0d3b-7daf-4777-e607-50192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Problems', 150
UNION ALL SELECT uuid '09eb1e4c-8eb0-4888-f718-60192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 2, 'GROUP', 'MODERATE_INTENSITY', 'Name Exploring Life’s Problems', 150
UNION ALL SELECT uuid '1afc2f5d-9fc1-4999-0829-70192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 3, 'GROUP', 'MODERATE_INTENSITY', 'Planning to Manage Life’s Problems', 150
UNION ALL SELECT uuid '2b0d306e-a1d2-48aa-193a-80192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 4, 'GROUP', 'MODERATE_INTENSITY', 'Putting it into Action', 150

-- 7) Managing Life’s Problems 1-1 (Individual); Moderate = 1
UNION ALL SELECT uuid 'a374b8e6-285a-4012-91b2-00182a3b4c5d', uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Life’s Problems 1-1', 60

-- 8) Managing People Around Me (Group); Moderate = 6
UNION ALL SELECT uuid 'c596da08-4a7c-4234-b3d4-20182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding the people and influences around me', 150
UNION ALL SELECT uuid 'd6a7eb19-5b8d-4345-c4e5-30182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 2, 'GROUP', 'MODERATE_INTENSITY', 'My role in relationships', 150
UNION ALL SELECT uuid 'e7b8fc2a-6c9e-4456-d5f6-40182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 3, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills', 150
UNION ALL SELECT uuid 'f8c90d3b-7daf-4567-e607-50182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 4, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills', 150
UNION ALL SELECT uuid '09da1e4c-8eb0-4678-f718-60182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 5, 'GROUP', 'MODERATE_INTENSITY', 'Practicing Our Relationship Skills', 150
UNION ALL SELECT uuid '1aeb2f5d-9fc1-4789-0829-70182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 6, 'GROUP', 'MODERATE_INTENSITY', 'Module Skills Practice', 150

-- 9) Managing People Around Me 1-1 (Individual); Moderate = 1
UNION ALL SELECT uuid 'b475c9f7-396b-4233-a2c3-10172a3b4c5d', uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing People Around Me 1-1', 60


-- 10) Bringing It All Together (Group); Moderate = 3
UNION ALL SELECT uuid 'd697eb19-5b8d-4455-c4e5-30172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 1, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Plan', 150
UNION ALL SELECT uuid 'e7a8fc2a-6c9e-4566-d5f6-40172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 2, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Practice', 150
UNION ALL SELECT uuid 'f8b90d3b-7daf-4677-e607-50172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 3, 'GROUP', 'MODERATE_INTENSITY', 'Programme Completion', 150
;

