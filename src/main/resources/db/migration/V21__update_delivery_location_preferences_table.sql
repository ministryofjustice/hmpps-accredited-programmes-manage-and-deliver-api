CREATE TABLE preferred_delivery_location_probation_delivery_unit
(
    id                 UUID PRIMARY KEY NOT NULL,
    delius_code        TEXT             NOT NULL,
    delius_description TEXT             NOT NULL
);

CREATE TABLE preferred_delivery_location
(
    id                                                     UUID PRIMARY KEY                                                         NOT NULL,
    delius_code                                            TEXT                                                                     NOT NULL,
    delius_description                                     TEXT                                                                     NOT NULL,
    preferred_delivery_location_probation_delivery_unit_id UUID REFERENCES preferred_delivery_location_probation_delivery_unit (id) NOT NULL,
    delivery_location_preferences_id                       UUID REFERENCES delivery_location_preferences (id)
);