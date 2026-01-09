CREATE TABLE attendee
(
    id          UUID NOT NULL PRIMARY KEY,
    person_name TEXT NOT NULL,
    referral_id UUID NOT NULL,
    session_id  UUID,
    CONSTRAINT fk_attendee_referral
        FOREIGN KEY (referral_id)
            REFERENCES referral (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_attendee_session
        FOREIGN KEY (session_id)
            REFERENCES session (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_attendee_referral_id ON attendee (referral_id);
CREATE INDEX idx_attendee_session_id ON attendee (session_id);

COMMENT ON TABLE attendee IS 'Represents an attendee of a session, linked to a referral.';
COMMENT ON COLUMN attendee.id IS 'Primary key.';
COMMENT ON COLUMN attendee.person_name IS 'Name of the attendee, matching the referral.';
COMMENT ON COLUMN attendee.referral_id IS 'FK to referral.id';
COMMENT ON COLUMN attendee.session_id IS 'FK to session.id (optional).';
