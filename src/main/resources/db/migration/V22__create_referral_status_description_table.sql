CREATE TABLE referral_status_description
(
    id                  UUID        PRIMARY KEY NOT NULL,
    description_text    TEXT        NOT NULL,
    is_closed           BOOLEAN     NOT NULL DEFAULT FALSE,
    label_colour        TEXT        NULL
);

COMMENT ON TABLE referral_status_description IS 'Contains the status descriptions for referrals';
COMMENT ON COLUMN referral_status_description.id IS 'Unique identifier for a referral status description';
COMMENT ON COLUMN referral_status_description.description_text IS 'The human-readable description of the status';
COMMENT ON COLUMN referral_status_description.is_closed IS 'Flag indicating whether this status represents a closed state';
COMMENT ON COLUMN referral_status_description.label_colour IS 'Optional colour code for UI display of the status label';