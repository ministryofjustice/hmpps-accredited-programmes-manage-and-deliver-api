CREATE TABLE referral_status_transition
(
    id                            UUID PRIMARY KEY                                       NOT NULL,
    from_status                   UUID REFERENCES referral_status_description (id)       NOT NULL,
    to_status                     UUID REFERENCES referral_status_description (id)       NOT NULL,
    description                   TEXT                                                   NULL,
    created_at                    TIMESTAMP                                              NULL,
    updated_at                    TIMESTAMP                                              NULL,
    deleted_at                    TIMESTAMP                                              NULL
);

-- Create indexes for performance
CREATE INDEX idx_referral_status_transition_from ON referral_status_transition (from_status);
CREATE INDEX idx_referral_status_transition_to ON referral_status_transition (to_status);

COMMENT ON TABLE referral_status_transition IS 'Contains available valid transitions between referral statuses';
COMMENT ON COLUMN referral_status_transition.id IS 'Unique identifier for a status transition';
COMMENT ON COLUMN referral_status_transition.from_status IS 'References the source status description';
COMMENT ON COLUMN referral_status_transition.to_status IS 'References the target status description';
COMMENT ON COLUMN referral_status_transition.description IS 'Optional description of the transition';
COMMENT ON COLUMN referral_status_transition.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN referral_status_transition.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN referral_status_transition.deleted_at IS 'Timestamp when the record was deleted (soft delete)';