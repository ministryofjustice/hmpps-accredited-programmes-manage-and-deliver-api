ALTER TABLE programme_group
    ADD COLUMN delivery_location TEXT,
    ADD COLUMN probation_delivery_unit TEXT,
    ADD COLUMN earliest_possible_start_date DATE,
    ADD COLUMN started_at_date DATE;

COMMENT ON COLUMN programme_group.earliest_possible_start_date IS 'The earliest date the group could start.';
COMMENT ON COLUMN programme_group.started_at_date IS 'The actual start date initiated by the facilitator';
