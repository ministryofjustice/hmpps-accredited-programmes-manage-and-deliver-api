CREATE TABLE delivery_location_preferences
(
    id                           UUID PRIMARY KEY                     NOT NULL,
    referral_id                  UUID references referral (id) UNIQUE NOT NULL,
    created_by                   TEXT      DEFAULT current_user       NOT NULL,
    created_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    last_updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    locations_cannot_attend_text TEXT                                 NULL
);

CREATE TABLE delivery_location_office_mapping
(
    delivery_location_id UUID references delivery_location_preferences (id),
    office_id            VARCHAR REFERENCES office (id) NOT NULL
);

COMMENT ON TABLE delivery_location_preferences IS 'Contains the delivery location preferences data for a referral';
COMMENT ON COLUMN delivery_location_preferences.id IS 'Unique identifier for a referral''s delivery location preferences';
COMMENT ON COLUMN delivery_location_preferences.referral_id IS 'References the unique identifier for a referral';
COMMENT ON COLUMN delivery_location_preferences.created_by IS 'The username of the person that submitted the delivery location preferences';
COMMENT ON COLUMN delivery_location_preferences.created_at IS 'Timestamp of when the delivery location preferences were submitted';
COMMENT ON COLUMN delivery_location_preferences.last_updated_at IS 'Timestamp of when the delivery location preferences were last updated';
COMMENT ON COLUMN delivery_location_preferences.locations_cannot_attend_text IS 'Details of locations a person cannot attend';

COMMENT ON TABLE delivery_location_office_mapping IS 'Contains the mapping between an office_id and a delivery location preference';
COMMENT ON COLUMN delivery_location_office_mapping.delivery_location_id IS 'The unique identifier of the delivery location preferences';
COMMENT ON COLUMN delivery_location_office_mapping.office_id IS 'The unique identifier of the reference data office id';