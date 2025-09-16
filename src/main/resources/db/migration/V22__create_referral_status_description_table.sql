CREATE TABLE referral_status_description
(
    id                  UUID        PRIMARY KEY NOT NULL,
    description_text    TEXT        NOT NULL,
    is_closed           BOOLEAN     NOT NULL DEFAULT FALSE,
    label_colour        TEXT        NULL,
    created_at          TIMESTAMP   NULL,
    updated_at          TIMESTAMP   NULL,
    deleted_at          TIMESTAMP   NULL
);

COMMENT ON TABLE referral_status_description IS 'Contains the status descriptions for referrals';
COMMENT ON COLUMN referral_status_description.id IS 'Unique identifier for a referral status description';
COMMENT ON COLUMN referral_status_description.description_text IS 'The human-readable description of the status';
COMMENT ON COLUMN referral_status_description.is_closed IS 'Flag indicating whether this status represents a closed state';
COMMENT ON COLUMN referral_status_description.label_colour IS 'Optional colour code for UI display of the status label';
COMMENT ON COLUMN referral_status_description.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN referral_status_description.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN referral_status_description.deleted_at IS 'Timestamp when the record was deleted (soft delete)';