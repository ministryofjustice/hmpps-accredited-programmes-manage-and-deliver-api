CREATE TABLE session_facilitator (
    session_id UUID NOT NULL,
    facilitator_id UUID NOT NULL,
    PRIMARY KEY (session_id, facilitator_id),
    CONSTRAINT fk_session_facilitator_session FOREIGN KEY (session_id) REFERENCES session (id) ON DELETE CASCADE,
    CONSTRAINT fk_session_facilitator_facilitator FOREIGN KEY (facilitator_id) REFERENCES facilitator (id) ON DELETE CASCADE
);

CREATE INDEX idx_session_facilitator_session_id ON session_facilitator (session_id);
CREATE INDEX idx_session_facilitator_facilitator_id ON session_facilitator (facilitator_id);

COMMENT ON TABLE session_facilitator IS 'Join table for sessions and facilitators';
