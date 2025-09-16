CREATE TABLE referral_status_transition
(
    referral_status_transition_id UUID PRIMARY KEY                                       NOT NULL,
    transition_from_status        UUID REFERENCES referral_status_description (id)       NOT NULL,
    transition_to_status          UUID REFERENCES referral_status_description (id)       NOT NULL,
    description                   TEXT                                                   NULL
);

-- Create indexes for performance
CREATE INDEX idx_referral_status_transition_from ON referral_status_transition (transition_from_status);
CREATE INDEX idx_referral_status_transition_to ON referral_status_transition (transition_to_status);

COMMENT ON TABLE referral_status_transition IS 'Contains available valid transitions between referral statuses';
COMMENT ON COLUMN referral_status_transition.referral_status_transition_id IS 'Unique identifier for a status transition';
COMMENT ON COLUMN referral_status_transition.transition_from_status IS 'References the source status description';
COMMENT ON COLUMN referral_status_transition.transition_to_status IS 'References the target status description';
COMMENT ON COLUMN referral_status_transition.description IS 'Optional description of the transition';