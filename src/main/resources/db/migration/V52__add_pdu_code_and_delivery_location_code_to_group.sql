ALTER TABLE programme_group
    ADD COLUMN probation_delivery_unit_code TEXT NULL,
    ADD COLUMN delivery_location_code       TEXT NULL,
    ADD CONSTRAINT chk_both_or_neither
        CHECK (
            (probation_delivery_unit_name IS NULL AND probation_delivery_unit_code IS NULL) OR
            (delivery_location_name IS NOT NULL AND delivery_location_code IS NOT NULL)
            );

COMMENT ON COLUMN programme_group.probation_delivery_unit_code IS 'The unique code in nDelius for the probation delivery unit.';
COMMENT ON COLUMN programme_group.delivery_location_code IS 'The unique code in nDelius for the delivery location.';
