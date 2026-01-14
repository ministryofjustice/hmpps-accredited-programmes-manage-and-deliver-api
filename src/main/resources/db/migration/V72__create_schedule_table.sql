ALTER TABLE session
    ADD COLUMN is_placeholder BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN session.is_placeholder IS 'If this session is a placeholder session.';

ALTER TABLE attendee
    DROP COLUMN person_name;