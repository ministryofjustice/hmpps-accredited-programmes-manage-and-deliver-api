ALTER TABLE programme_group
DROP
CONSTRAINT chk_both_or_neither;

ALTER TABLE programme_group
    ADD CONSTRAINT chk_both_or_neither
        CHECK ((
                   (probation_delivery_unit_name IS NULL AND probation_delivery_unit_code IS NULL) OR
                   (probation_delivery_unit_name IS NOT NULL AND probation_delivery_unit_code IS NOT NULL)
                   )
            AND
               (
                   (delivery_location_name IS NULL AND delivery_location_code IS NULL) OR
                   (delivery_location_name IS NOT NULL AND delivery_location_code IS NOT NULL)
                   )
            );