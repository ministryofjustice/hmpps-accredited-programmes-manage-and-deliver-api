ALTER TABLE session
    ADD COLUMN session_facilitator_id UUID;

ALTER TABLE session
    ADD CONSTRAINT fk_session_facilitator
        FOREIGN KEY (session_facilitator_id)
            REFERENCES facilitator (id)
            ON DELETE SET NULL;

CREATE INDEX idx_session_session_facilitator_id ON session (session_facilitator_id);

COMMENT ON COLUMN session.session_facilitator_id IS 'FK to facilitator.id';
