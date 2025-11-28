-- Seed data for Accredited Programme Template: "Building Choices"
-- Populates accredited_programme_template, module, and module_session_template

-- Create the accredited_programme_template
INSERT INTO accredited_programme_template (id, name, valid_from, valid_until)
VALUES (uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Building Choices', CURRENT_DATE, NULL);

-- Insert modules individually (explicit ids, keep module_number ordering)
INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Pre-Group', 1);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Ready', 2);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '9a3bd4c5-2e6f-4f81-9c3b-5d6e7f8a9b01', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Ready 1-1', 3);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Started', 4);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Getting Started 1-1', 5);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Myself', 6);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Myself 1-1', 7);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Life’s Problems', 8);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing Life’s Problems 1-1', 9);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing People Around Me', 10);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Managing People Around Me 1-1', 11);

INSERT INTO module (id, accredited_programme_template_id, name, module_number)
VALUES (uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', uuid '3442732f-9a0d-4981-8f9e-54e622e72211', 'Bringing It All Together', 12);

-- Insert session templates (both pathways) for each module
INSERT INTO module_session_template (id, module_id, session_number, session_type, pathway, name)

-- 1) Pre-Group (Individual); Moderate = 1 , High = 1
SELECT uuid '1bcaf371-e624-4034-a13b-5ae2e9921bd4', uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Pre-Group'
UNION ALL
SELECT uuid '29eec5f6-6264-4594-bc25-87d462e7fe6d', uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Pre-Group'

-- 2) Getting Ready (Group); Moderate = 0; High = 15
UNION ALL SELECT uuid 'a1d0f9c0-4e2a-4b9f-9a26-2b9f6d3e8d10', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 1, 'GROUP', 'HIGH_INTENSITY', 'Getting to know Getting Ready'
UNION ALL SELECT uuid 'b2e3a7d5-1c4f-4f21-8d0a-9f6b2a1c3d45', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 2, 'GROUP', 'HIGH_INTENSITY', 'Getting to know each other'
UNION ALL SELECT uuid 'c3f5b8e6-2d5a-4c32-9e1b-0a1b2c3d4e56', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 3, 'GROUP', 'HIGH_INTENSITY', 'Taking time out'
UNION ALL SELECT uuid 'd4a6c9f7-3e6b-4d43-a2c3-1b2c3d4e5f67', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 4, 'GROUP', 'HIGH_INTENSITY', 'Taking care of'
UNION ALL SELECT uuid 'e5b7da08-4f7c-4e54-b3d4-2c3d4e5f6078', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 5, 'GROUP', 'HIGH_INTENSITY', 'What makes us who we are'
UNION ALL SELECT uuid 'f6c8eb19-5a8d-4f65-c4e5-3d4e5f607189', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 6, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: What happened in my life'
UNION ALL SELECT uuid 'a7d9fc2a-6b9e-4066-d5f6-4e5f6071892a', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 7, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: What I learned'
UNION ALL SELECT uuid 'b8ea0d3b-7caf-4177-e607-5f6071892a3b', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 8, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: How it impacts my life'
UNION ALL SELECT uuid 'c9fb1e4c-8db0-4288-f718-6071892a3b4c', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 9, 'GROUP', 'HIGH_INTENSITY', 'What matters to me (My values)'
UNION ALL SELECT uuid 'da0c2f5d-9ec1-4399-0829-71892a3b4c5d', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 10, 'GROUP', 'HIGH_INTENSITY', 'What I’m good at (my strengths)'
UNION ALL SELECT uuid 'eb1d306e-a0d2-44aa-193a-8292a3b4c5d6', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 11, 'GROUP', 'HIGH_INTENSITY', 'My strong thoughts (my beliefs)'
UNION ALL SELECT uuid 'fc2e417f-b1e3-45bb-2a4b-92a3b4c5d6e7', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 12, 'GROUP', 'HIGH_INTENSITY', 'What gets in the way'
UNION ALL SELECT uuid 'ad3f5280-c2f4-46cc-3b5c-a3b4c5d6e7f8', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 13, 'GROUP', 'HIGH_INTENSITY', 'When the going gets tough'
UNION ALL SELECT uuid 'be406391-d305-47dd-4c6d-b4c5d6e7f809', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 14, 'GROUP', 'HIGH_INTENSITY', 'Coming to an end'
UNION ALL SELECT uuid 'cf5174a2-e416-48ee-5d7e-c5d6e7f8091a', uuid '7f2a5d3e-9b1c-4d52-8a4a-3a7f2b9c6e10', 15, 'GROUP', 'HIGH_INTENSITY', 'Moving on to the rest of Building Choices'

-- 3) Getting Ready 1-1 (Individual); Moderate=0; High=1
UNION ALL SELECT uuid 'd06285b3-f527-49ff-6e8f-d6e7f8091a2b', uuid '9a3bd4c5-2e6f-4f81-9c3b-5d6e7f8a9b01', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Getting Ready 1-1'

-- 4) Getting Started (Group); Moderate = 2, High = 2
UNION ALL SELECT uuid 'e17396c4-0638-4100-7f90-e7f8091a2b3c', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 1, 'GROUP', 'MODERATE_INTENSITY', 'Introduction to Building Choices'
UNION ALL SELECT uuid 'f284a7d5-1749-4211-80a1-f8091a2b3c4d', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 2, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Myself'
UNION ALL SELECT uuid 'a395b8e6-285a-4322-91b2-091a2b3c4d5e', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 1, 'GROUP', 'HIGH_INTENSITY', 'Introduction to Building Choices'
UNION ALL SELECT uuid 'b4a6c9f7-396b-4433-a2c3-1a2b3c4d5e6f', uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c', 2, 'GROUP', 'HIGH_INTENSITY', 'Understanding Myself'

-- 5) Getting Started 1-1 (Individual); Moderate = 1; High = 1
UNION ALL SELECT uuid 'c5b7da08-4a7c-4544-b3d4-2b3c4d5e6f70', uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Getting Started 1-1'
UNION ALL SELECT uuid 'd6c8eb19-5b8d-4655-c4e5-3c4d5e6f7081', uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Getting Started 1-1'

-- 6) Managing Myself (Group); Moderate = 6; High = 9
UNION ALL SELECT uuid 'e7d9fc2a-6c9e-4766-d5f6-4d5e6f708192', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding my feelings'
UNION ALL SELECT uuid 'f8ea0d3b-7daf-4877-e607-5e6f708192a3', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 2, 'GROUP', 'MODERATE_INTENSITY', 'Helpful/Unhelpful Feelings'
UNION ALL SELECT uuid '09fb1e4c-8eb0-4988-f718-6f708192a3b4', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 3, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 1'
UNION ALL SELECT uuid '1a0c2f5d-9fc1-4a99-0829-70708192a3b4', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 4, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 2'
UNION ALL SELECT uuid '2b1d306e-a1d2-4baa-193a-808192a3b4c5', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 5, 'GROUP', 'MODERATE_INTENSITY', 'Understanding My Thinking'
UNION ALL SELECT uuid '3c2e417f-b2e3-4bbb-2a4b-90192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 6, 'GROUP', 'MODERATE_INTENSITY', 'Developing My Flexible Thinking'
UNION ALL SELECT uuid '4d3f5280-c3f4-4ccc-3b5c-a0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding my feelings'
UNION ALL SELECT uuid '5e406391-d405-4ddd-4c6d-b0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 2, 'GROUP', 'HIGH_INTENSITY', 'Helpful/Unhelpful Feelings'
UNION ALL SELECT uuid '6f5174a2-e516-4eee-5d7e-c0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 3, 'GROUP', 'HIGH_INTENSITY', 'Managing My Feelings - Part 1'
UNION ALL SELECT uuid '705285b3-f627-4fff-6e8f-d0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 4, 'GROUP', 'HIGH_INTENSITY', 'Managing My Feelings - Part 2'
UNION ALL SELECT uuid '816396c4-0638-4000-7f90-e0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 5, 'GROUP', 'HIGH_INTENSITY', 'Understanding My Thinking'
UNION ALL SELECT uuid '9274a7d5-1749-4111-80a1-f0192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 6, 'GROUP', 'HIGH_INTENSITY', 'Developing My Flexible Thinking'
UNION ALL SELECT uuid 'a385b8e6-285a-4222-91b2-00192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT uuid 'b496c9f7-396b-4333-a2c3-10192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 8, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT uuid 'c5a7da08-4a7c-4444-b3d4-20192a3b4c5d', uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e', 9, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 7) Managing Myself 1-1 (Individual); Moderate=1; High=1
UNION ALL SELECT uuid 'd6b8eb19-5b8d-4555-c4e5-30192a3b4c5d', uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Myself 1-1'
UNION ALL SELECT uuid 'e7c9fc2a-6c9e-4666-d5f6-40192a3b4c5d', uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing Myself 1-1'

-- 8) Managing Life’s Problems (Group); Moderate=list (4); High=list (7)
UNION ALL SELECT uuid 'f8da0d3b-7daf-4777-e607-50192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Problems'
UNION ALL SELECT uuid '09eb1e4c-8eb0-4888-f718-60192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 2, 'GROUP', 'MODERATE_INTENSITY', 'Name Exploring Life’s Problems'
UNION ALL SELECT uuid '1afc2f5d-9fc1-4999-0829-70192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 3, 'GROUP', 'MODERATE_INTENSITY', 'Planning to Manage Life’s Problems'
UNION ALL SELECT uuid '2b0d306e-a1d2-48aa-193a-80192a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 4, 'GROUP', 'MODERATE_INTENSITY', 'Putting it into Action'
UNION ALL SELECT uuid '3c1e417f-b2e3-49bb-2a4b-90182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding Problems'
UNION ALL SELECT uuid '4d2f5280-c3f4-4acc-3b5c-a0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 2, 'GROUP', 'HIGH_INTENSITY', 'Name Exploring Life’s Problems'
UNION ALL SELECT uuid '5e306391-d405-4bdd-4c6d-b0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 3, 'GROUP', 'HIGH_INTENSITY', 'Planning to Manage Life’s Problems'
UNION ALL SELECT uuid '6f4174a2-e516-4ced-5d7e-c0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 4, 'GROUP', 'HIGH_INTENSITY', 'Putting it into Action'
UNION ALL SELECT uuid '704285b3-f627-4dfd-6e8f-d0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 5, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT uuid '815396c4-0638-4e00-7f90-e0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 6, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT uuid '9264a7d5-1749-4f11-80a1-f0182a3b4c5d', uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a', 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 9) Managing Life’s Problems 1-1 (Individual); Moderate=1; High=1
UNION ALL SELECT uuid 'a374b8e6-285a-4012-91b2-00182a3b4c5d', uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Life’s Problems 1-1'
UNION ALL SELECT uuid 'b485c9f7-396b-4123-a2c3-10182a3b4c5d', uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing Life’s Problems 1-1'

-- 10) Managing People Around Me (Group); Moderate = 6) High = 9
UNION ALL SELECT uuid 'c596da08-4a7c-4234-b3d4-20182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding the people and influences around me'
UNION ALL SELECT uuid 'd6a7eb19-5b8d-4345-c4e5-30182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 2, 'GROUP', 'MODERATE_INTENSITY', 'My role in relationships'
UNION ALL SELECT uuid 'e7b8fc2a-6c9e-4456-d5f6-40182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 3, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills'
UNION ALL SELECT uuid 'f8c90d3b-7daf-4567-e607-50182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 4, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills'
UNION ALL SELECT uuid '09da1e4c-8eb0-4678-f718-60182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 5, 'GROUP', 'MODERATE_INTENSITY', 'Practicing Our Relationship Skills'
UNION ALL SELECT uuid '1aeb2f5d-9fc1-4789-0829-70182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 6, 'GROUP', 'MODERATE_INTENSITY', 'Module Skills Practice'
UNION ALL SELECT uuid '2bfc306e-a1d2-489a-193a-80182a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding the people and influences around me'
UNION ALL SELECT uuid '3c0d417f-b2e3-49ab-2a4b-90172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 2, 'GROUP', 'HIGH_INTENSITY', 'My role in relationships'
UNION ALL SELECT uuid '4d1e5280-c3f4-4bcb-3b5c-a0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 3, 'GROUP', 'HIGH_INTENSITY', 'Relationship Skills'
UNION ALL SELECT uuid '5e2f6391-d405-4cdc-4c6d-b0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 4, 'GROUP', 'HIGH_INTENSITY', 'Relationship Skills'
UNION ALL SELECT uuid '6f3074a2-e516-4ded-5d7e-c0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 5, 'GROUP', 'HIGH_INTENSITY', 'Practicing Our Relationship Skills'
UNION ALL SELECT uuid '703185b3-f627-4efe-6e8f-d0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 6, 'GROUP', 'HIGH_INTENSITY', 'Module Skills Practice'
UNION ALL SELECT uuid '814296c4-0638-4f00-7f90-e0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT uuid '9253a7d5-1749-4011-80a1-f0172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 8, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT uuid 'a364b8e6-285a-4122-91b2-00172a3b4c5d', uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c', 9, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 11) Managing People Around Me 1-1 (Individual); Moderate = 1; High = 1
UNION ALL SELECT uuid 'b475c9f7-396b-4233-a2c3-10172a3b4c5d', uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d', 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing People Around Me 1-1'
UNION ALL SELECT uuid 'c586da08-4a7c-4344-b3d4-20172a3b4c5d', uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d', 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing People Around Me 1-1'

-- 12) Bringing It All Together (Group); Moderate = 3; High = 4
UNION ALL SELECT uuid 'd697eb19-5b8d-4455-c4e5-30172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 1, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Plan'
UNION ALL SELECT uuid 'e7a8fc2a-6c9e-4566-d5f6-40172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 2, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Practice'
UNION ALL SELECT uuid 'f8b90d3b-7daf-4677-e607-50172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 3, 'GROUP', 'MODERATE_INTENSITY', 'Programme Completion'
UNION ALL SELECT uuid '09ca1e4c-8eb0-4788-f718-60172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 1, 'GROUP', 'HIGH_INTENSITY', 'Future Me Plan'
UNION ALL SELECT uuid '1adb2f5d-9fc1-4899-0829-70172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 2, 'GROUP', 'HIGH_INTENSITY', 'Future Me Practice - Part 1'
UNION ALL SELECT uuid '2bec306e-a1d2-49aa-193a-80172a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 3, 'GROUP', 'HIGH_INTENSITY', 'Future Me Practice - Part 2'
UNION ALL SELECT uuid '3cdd417f-b2e3-4abb-2a4b-90162a3b4c5d', uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e', 4, 'GROUP', 'HIGH_INTENSITY', 'Programme Completion'
;

