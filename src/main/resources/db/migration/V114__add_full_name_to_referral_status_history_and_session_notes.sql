ALTER TABLE referral_status_history
    ADD COLUMN created_by_full_name VARCHAR(150) DEFAULT NULL;

COMMENT ON COLUMN referral_status_history.created_by_full_name IS 'Free text input, providing full name of a user that created referral status history.';

ALTER TABLE session_notes_history
    ADD COLUMN created_by_full_name VARCHAR(150) DEFAULT NULL;

COMMENT ON COLUMN session_notes_history.created_by_full_name IS 'Free text input, providing full name of a user that created session notes history.';
