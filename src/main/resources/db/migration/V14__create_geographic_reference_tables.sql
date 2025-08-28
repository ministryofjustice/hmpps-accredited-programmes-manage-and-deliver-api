-- Create tables for geographic reference data (Regions, PDUs, and Offices),
-- used initially in Delivery Location Preferences
-- For more information see /docs/schema-docs/2025-08-26-geographic-reference-data.md

CREATE TABLE IF NOT EXISTS region (
    id VARCHAR NOT NULL PRIMARY KEY,
    region_name VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);


-- PDU: Probation Delivery Unit
CREATE TABLE IF NOT EXISTS pdu (
    id INTEGER NOT NULL PRIMARY KEY,
    pdu_name VARCHAR NOT NULL,
    region_id VARCHAR NOT NULL,
    ndelius_pdu_code VARCHAR,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_pdu_region FOREIGN KEY (region_id) REFERENCES region (id)
);

CREATE TABLE IF NOT EXISTS office (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL,
    office_name VARCHAR NOT NULL,
    office_address VARCHAR,
    pdu_id INTEGER NOT NULL,
    region_id VARCHAR NOT NULL,
    delius_crs_location_id VARCHAR,
    is_delivery_office BOOLEAN NOT NULL DEFAULT FALSE,
    is_reporting_office BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_office_pdu FOREIGN KEY (pdu_id) REFERENCES pdu (id),
    CONSTRAINT fk_office_region FOREIGN KEY (region_id) REFERENCES region (id)
);

CREATE INDEX IF NOT EXISTS idx_region_name ON region(region_name);
CREATE INDEX IF NOT EXISTS idx_region_id ON region(id);

CREATE INDEX IF NOT EXISTS idx_pdu_region_id ON pdu(region_id);
CREATE INDEX IF NOT EXISTS idx_pdu_name ON pdu(pdu_name);

CREATE INDEX IF NOT EXISTS idx_office_pdu_id ON office(pdu_id);
CREATE INDEX IF NOT EXISTS idx_office_region_id ON office(region_id);
CREATE INDEX IF NOT EXISTS idx_office_name ON office(name);
