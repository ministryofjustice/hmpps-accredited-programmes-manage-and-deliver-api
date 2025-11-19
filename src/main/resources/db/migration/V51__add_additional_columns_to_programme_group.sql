ALTER TABLE programme_group
    ADD COLUMN delivery_location_name TEXT,
    ADD COLUMN probation_delivery_unit_name TEXT,
    ADD COLUMN earliest_possible_start_date DATE;

COMMENT ON COLUMN programme_group.earliest_possible_start_date IS 'The earliest date the group could start in UTC+0.';
