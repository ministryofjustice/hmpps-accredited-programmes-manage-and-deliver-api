ALTER TABLE referral_status_history
    ADD COLUMN additional_details TEXT;

ALTER TABLE referral_status_history
    DROP COLUMN end_date;

COMMENT ON COLUMN referral_status_history.additional_details IS 'Free text input from the user, explaining, or providing context for the change in Status.';