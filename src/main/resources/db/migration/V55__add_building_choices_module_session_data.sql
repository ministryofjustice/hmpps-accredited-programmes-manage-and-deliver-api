-- Seed data for Accredited Programme Template: "Building Choices"
-- Populates accredited_programme_template, module, and module_session_template

-- Create the accredited_programme_template and capture its id
WITH template_row AS (
    INSERT INTO accredited_programme_template (id, name, valid_from, valid_until)
    VALUES (gen_random_uuid(), 'Building Choices', CURRENT_DATE, NULL)
    RETURNING id
),
-- Insert modules in order, assigning module_number sequentially
modules AS (
    INSERT INTO module (id, accredited_programme_template_id, name, module_number)
    SELECT gen_random_uuid(), template_row.id, m.name, m.num
    FROM template_row
    JOIN (VALUES
              ('Pre-Group', 1),
              ('Getting Ready', 2),
              ('Getting Ready 1-1', 3),
              ('Getting Started', 4),
              ('Getting Started 1-1', 5),
              ('Managing Myself', 6),
              ('Managing Myself 1-1', 7),
              ('Managing Life’s Problems', 8),
              ('Managing Life’s Problems 1-1', 9),
              ('Managing People Around Me', 10),
              ('Managing People Around Me 1-1', 11),
              ('Bringing It All Together', 12)
         ) AS m(name, num)
         ON TRUE
    RETURNING id, name
)
-- Insert session templates (both pathways) for each module
INSERT INTO module_session_template (id, module_id, session_number, session_type, pathway, name)

-- 1) Pre-Group (Individual); Moderate = 1 , High = 1
SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Pre-Group'), 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Pre-Group'
UNION ALL
SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Pre-Group'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Pre-Group'

-- 2) Getting Ready (Group); Moderate = 0; High = 15
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 1, 'GROUP', 'HIGH_INTENSITY', 'Getting to know Getting Ready'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 2, 'GROUP', 'HIGH_INTENSITY', 'Getting to know each other'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 3, 'GROUP', 'HIGH_INTENSITY', 'Taking time out'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 4, 'GROUP', 'HIGH_INTENSITY', 'Taking care of'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 5, 'GROUP', 'HIGH_INTENSITY', 'What makes us who we are'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 6, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: What happened in my life'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 7, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: What I learned'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 8, 'GROUP', 'HIGH_INTENSITY', 'Getting to know myself: How it impacts my life'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 9, 'GROUP', 'HIGH_INTENSITY', 'What matters to me (My values)'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 10, 'GROUP', 'HIGH_INTENSITY', 'What I’m good at (my strengths)'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 11, 'GROUP', 'HIGH_INTENSITY', 'My strong thoughts (my beliefs)'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 12, 'GROUP', 'HIGH_INTENSITY', 'What gets in the way'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 13, 'GROUP', 'HIGH_INTENSITY', 'When the going gets tough'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 14, 'GROUP', 'HIGH_INTENSITY', 'Coming to an end'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready'), 15, 'GROUP', 'HIGH_INTENSITY', 'Moving on to the rest of Building Choices'

-- 3) Getting Ready 1-1 (Individual); Moderate=0; High=1
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Ready 1-1'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Getting Ready 1-1'

-- 4) Getting Started (Group); Moderate = 2, High = 2
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started'), 1, 'GROUP', 'MODERATE_INTENSITY', 'Introduction to Building Choices'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started'), 2, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Myself'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started'), 1, 'GROUP', 'HIGH_INTENSITY', 'Introduction to Building Choices'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started'), 2, 'GROUP', 'HIGH_INTENSITY', 'Understanding Myself'

-- 5) Getting Started 1-1 (Individual); Moderate = 1; High = 1
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started 1-1'), 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Getting Started 1-1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Getting Started 1-1'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Getting Started 1-1'

-- 6) Managing Myself (Group); Moderate = 6; High = 9
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding my feelings'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 2, 'GROUP', 'MODERATE_INTENSITY', 'Helpful/Unhelpful Feelings'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 3, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 4, 'GROUP', 'MODERATE_INTENSITY', 'Managing My Feelings - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 5, 'GROUP', 'MODERATE_INTENSITY', 'Understanding My Thinking'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 6, 'GROUP', 'MODERATE_INTENSITY', 'Developing My Flexible Thinking'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding my feelings'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 2, 'GROUP', 'HIGH_INTENSITY', 'Helpful/Unhelpful Feelings'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 3, 'GROUP', 'HIGH_INTENSITY', 'Managing My Feelings - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 4, 'GROUP', 'HIGH_INTENSITY', 'Managing My Feelings - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 5, 'GROUP', 'HIGH_INTENSITY', 'Understanding My Thinking'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 6, 'GROUP', 'HIGH_INTENSITY', 'Developing My Flexible Thinking'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 8, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself'), 9, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 7) Managing Myself 1-1 (Individual); Moderate=1; High=1
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself 1-1'), 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Myself 1-1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Myself 1-1'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing Myself 1-1'

-- 8) Managing Life’s Problems (Group); Moderate=list (4); High=list (7)
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 2, 'GROUP', 'MODERATE_INTENSITY', 'Name Exploring Life’s Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 3, 'GROUP', 'MODERATE_INTENSITY', 'Planning to Manage Life’s Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 4, 'GROUP', 'MODERATE_INTENSITY', 'Putting it into Action'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 2, 'GROUP', 'HIGH_INTENSITY', 'Name Exploring Life’s Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 3, 'GROUP', 'HIGH_INTENSITY', 'Planning to Manage Life’s Problems'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 4, 'GROUP', 'HIGH_INTENSITY', 'Putting it into Action'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 5, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 6, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems'), 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 9) Managing Life’s Problems 1-1 (Individual); Moderate=1; High=1
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems 1-1'), 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing Life’s Problems 1-1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing Life’s Problems 1-1'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing Life’s Problems 1-1'

-- 10) Managing People Around Me (Group); Moderate=list (6); High=list (9)
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 1, 'GROUP', 'MODERATE_INTENSITY', 'Understanding the people and influences around me'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 2, 'GROUP', 'MODERATE_INTENSITY', 'My role in relationships'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 3, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 4, 'GROUP', 'MODERATE_INTENSITY', 'Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 5, 'GROUP', 'MODERATE_INTENSITY', 'Practicing Our Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 6, 'GROUP', 'MODERATE_INTENSITY', 'Module Skills Practice'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 1, 'GROUP', 'HIGH_INTENSITY', 'Understanding the people and influences around me'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 2, 'GROUP', 'HIGH_INTENSITY', 'My role in relationships'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 3, 'GROUP', 'HIGH_INTENSITY', 'Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 4, 'GROUP', 'HIGH_INTENSITY', 'Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 5, 'GROUP', 'HIGH_INTENSITY', 'Practicing Our Relationship Skills'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 6, 'GROUP', 'HIGH_INTENSITY', 'Module Skills Practice'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 7, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 8, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me'), 9, 'GROUP', 'HIGH_INTENSITY', 'Future Me Skills - Part 3'

-- 11) Managing People Around Me 1-1 (Individual); Moderate=1; High=1
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me 1-1'), 1, 'ONE_TO_ONE', 'MODERATE_INTENSITY', 'Managing People Around Me 1-1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Managing People Around Me 1-1'), 1, 'ONE_TO_ONE', 'HIGH_INTENSITY', 'Managing People Around Me 1-1'

-- 12) Bringing It All Together (Group); Moderate=list (3); High=list (4)
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 1, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Plan'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 2, 'GROUP', 'MODERATE_INTENSITY', 'Future Me Practice'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 3, 'GROUP', 'MODERATE_INTENSITY', 'Programme Completion'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 1, 'GROUP', 'HIGH_INTENSITY', 'Future Me Plan'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 2, 'GROUP', 'HIGH_INTENSITY', 'Future Me Practice - Part 1'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 3, 'GROUP', 'HIGH_INTENSITY', 'Future Me Practice - Part 2'
UNION ALL SELECT gen_random_uuid(), (SELECT id FROM modules WHERE name = 'Bringing It All Together'), 4, 'GROUP', 'HIGH_INTENSITY', 'Programme Completion'
;

