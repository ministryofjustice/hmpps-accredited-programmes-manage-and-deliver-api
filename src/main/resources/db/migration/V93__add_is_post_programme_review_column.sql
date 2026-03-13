CREATE TYPE session_role AS ENUM ('PRE_GROUP','STANDARD','POST_PROGRAMME');

ALTER TABLE module_session_template
    ADD COLUMN session_role session_role NOT NULL DEFAULT 'STANDARD';

COMMENT ON COLUMN module_session_template.session_role IS 'The role that this session is in relation to the entire programme';

UPDATE module_session_template
SET session_role = 'PRE_GROUP'
where name = 'Pre-group one-to-one';

UPDATE module_session_template
SET session_role = 'POST_PROGRAMME'
where name = 'Post-programme review';

