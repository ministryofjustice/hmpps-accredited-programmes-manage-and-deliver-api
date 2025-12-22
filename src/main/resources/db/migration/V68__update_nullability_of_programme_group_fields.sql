ALTER TABLE programme_group
    ALTER COLUMN earliest_possible_start_date SET NOT NULL,
    ALTER COLUMN probation_delivery_unit_name SET NOT NULL,
    ALTER COLUMN probation_delivery_unit_code SET NOT NULL,
    ALTER COLUMN delivery_location_name SET NOT NULL,
    ALTER COLUMN delivery_location_code SET NOT NULL;