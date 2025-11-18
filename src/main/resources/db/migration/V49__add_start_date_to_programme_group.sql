ALTER TABLE programme_group
    ADD COLUMN started_at_date DATE;

CREATE INDEX idx_programme_group_start_at_date ON programme_group (started_at_date);

COMMENT ON COLUMN programme_group.started_at_date IS 'The date when the programme group started';