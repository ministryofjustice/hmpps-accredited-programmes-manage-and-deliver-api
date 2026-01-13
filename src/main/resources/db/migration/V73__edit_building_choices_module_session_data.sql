--Correcting casing and changing 1-1 to be one-to-one

UPDATE module
SET name = CASE id
               WHEN uuid '33a740fb-a7b0-42e3-ba6b-e2b3ec25c795' THEN 'Pre-group'
               WHEN uuid '12c3d4e5-6f78-49a0-8b1c-2d3e4f5a6b7c' THEN 'Getting started'
               WHEN uuid '23d4e5f6-7a89-4ab1-9c2d-3e4f5a6b7c8d' THEN 'Getting started one-to-one'
               WHEN uuid '34e5f6a7-8b9a-4bc2-8d3e-4f5a6b7c8d9e' THEN 'Managing myself'
               WHEN uuid '45f6a7b8-9c0a-4cd3-9e4f-5a6b7c8d9e0f' THEN 'Managing myself one-to-one'
               WHEN uuid '56a7b8c9-0a1b-4de4-8051-6b7c8d9e0f1a' THEN 'Managing life’s problems'
               WHEN uuid '67b8c9d0-1a2b-4ef5-9152-7c8d9e0f1a2b' THEN 'Managing life’s problems one-to-one'
               WHEN uuid '78c9d0e1-2a3b-40f6-a163-8d9e0f1a2b3c' THEN 'Managing people around me'
               WHEN uuid '89d0e1f2-3a4b-4017-b274-9e0f1a2b3c4d' THEN 'Managing people around me one-to-one'
               WHEN uuid '9ad0e1f2-4a5b-4028-c385-0f1a2b3c4d5e' THEN 'Bringing it all together'
    END;

UPDATE module_session_template
SET name = CASE id
    -- 1) Pre-Group
               WHEN uuid '1bcaf371-e624-4034-a13b-5ae2e9921bd4' THEN 'Pre-group'

    -- 2) Getting Started
               WHEN uuid 'e17396c4-0638-4100-7f90-e7f8091a2b3c' THEN 'Introduction to building choices'
               WHEN uuid 'f284a7d5-1749-4211-80a1-f8091a2b3c4d' THEN 'Understanding myself'

    -- 3) Getting Started one-to-one
               WHEN uuid 'c5b7da08-4a7c-4544-b3d4-2b3c4d5e6f70' THEN 'Getting started one-to-one'

    -- 4) Managing Myself
               WHEN uuid 'e7d9fc2a-6c9e-4766-d5f6-4d5e6f708192' THEN 'Understanding my feelings'
               WHEN uuid 'f8ea0d3b-7daf-4877-e607-5e6f708192a3' THEN 'Helpful/unhelpful feelings'
               WHEN uuid '09fb1e4c-8eb0-4988-f718-6f708192a3b4' THEN 'Managing my feelings - part 1'
               WHEN uuid '1a0c2f5d-9fc1-4a99-0829-70708192a3b4' THEN 'Managing my feelings - part 2'
               WHEN uuid '2b1d306e-a1d2-4baa-193a-808192a3b4c5' THEN 'Understanding my thinking'
               WHEN uuid '3c2e417f-b2e3-4bbb-2a4b-90192a3b4c5d' THEN 'Developing my flexible thinking'

    -- 5) Managing Myself one-to-one
               WHEN uuid 'd6b8eb19-5b8d-4555-c4e5-30192a3b4c5d' THEN 'Managing myself one-to-one'

    -- 6) Managing Life's Problems
               WHEN uuid 'f8da0d3b-7daf-4777-e607-50192a3b4c5d' THEN 'Understanding problems'
               WHEN uuid '09eb1e4c-8eb0-4888-f718-60192a3b4c5d' THEN 'Exploring life’s problems'
               WHEN uuid '1afc2f5d-9fc1-4999-0829-70192a3b4c5d' THEN 'Planning to manage life’s problems'
               WHEN uuid '2b0d306e-a1d2-48aa-193a-80192a3b4c5d' THEN 'Putting it into action'

    -- 7) Managing Life's Problems one-to-one
               WHEN uuid 'a374b8e6-285a-4012-91b2-00182a3b4c5d' THEN 'Managing life’s problems one-to-one'

    -- 8) Managing People Around Me
               WHEN uuid 'c596da08-4a7c-4234-b3d4-20182a3b4c5d' THEN 'Understanding the people and influences around me'
               WHEN uuid 'd6a7eb19-5b8d-4345-c4e5-30182a3b4c5d' THEN 'My role in relationships'
               WHEN uuid 'e7b8fc2a-6c9e-4456-d5f6-40182a3b4c5d' THEN 'Relationship skills'
               WHEN uuid 'f8c90d3b-7daf-4567-e607-50182a3b4c5d' THEN 'Relationship skills'
               WHEN uuid '09da1e4c-8eb0-4678-f718-60182a3b4c5d' THEN 'Practicing our relationship skills'
               WHEN uuid '1aeb2f5d-9fc1-4789-0829-70182a3b4c5d' THEN 'Module skills practice'

    -- 9) Managing People Around Me one-to-one
               WHEN uuid 'b475c9f7-396b-4233-a2c3-10172a3b4c5d' THEN 'Managing people around me one-to-one'

    -- 10) Bringing It All Together
               WHEN uuid 'd697eb19-5b8d-4455-c4e5-30172a3b4c5d' THEN 'Future me plan'
               WHEN uuid 'e7a8fc2a-6c9e-4566-d5f6-40172a3b4c5d' THEN 'Future me practice'
               WHEN uuid 'f8b90d3b-7daf-4677-e607-50172a3b4c5d' THEN 'Programme completion'
    END;