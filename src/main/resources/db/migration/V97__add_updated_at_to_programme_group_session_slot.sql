ALTER TABLE programme_group_session_slot
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN programme_group_session_slot.updated_at IS 'programme group session slot last updated time';