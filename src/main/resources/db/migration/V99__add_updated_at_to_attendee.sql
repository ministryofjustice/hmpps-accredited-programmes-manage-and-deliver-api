ALTER TABLE attendee
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
COMMENT ON COLUMN attendee.updated_at IS 'attendee last updated time';
