-- Create tables for geographic reference data (Regions, PDUs, and Offices),
-- used initially in Delivery Location Preferences
-- For more information see /docs/schema-docs/2025-08-26-geographic-reference-data.md

CREATE TABLE IF NOT EXISTS region (
    probation_region_id VARCHAR(10) NOT NULL PRIMARY KEY,
    region_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- PDU: Probation Delivery Unit
CREATE TABLE IF NOT EXISTS pdu (
    pdu_id INTEGER NOT NULL PRIMARY KEY,
    pdu_name VARCHAR(255) NOT NULL,
    probation_region_id VARCHAR(10) NOT NULL,
    ndelius_pdu_code VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_pdu_region FOREIGN KEY (probation_region_id) REFERENCES region (probation_region_id)
);

CREATE TABLE IF NOT EXISTS office (
    probation_office_id VARCHAR(20) NOT NULL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    office_name VARCHAR(255) NOT NULL,
    office_address VARCHAR(1000),
    pdu_id INTEGER NOT NULL,
    probation_region_id VARCHAR(10) NOT NULL,
    delius_crs_location_id VARCHAR(50),
    is_delivery_office BOOLEAN NOT NULL DEFAULT FALSE,
    is_reporting_office BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_office_pdu FOREIGN KEY (pdu_id) REFERENCES pdu (pdu_id),
    CONSTRAINT fk_office_region FOREIGN KEY (probation_region_id) REFERENCES region (probation_region_id)
);

CREATE INDEX IF NOT EXISTS idx_region_name ON region(region_name);
CREATE INDEX IF NOT EXISTS idx_region_probation_region_id ON region(probation_region_id);

CREATE INDEX IF NOT EXISTS idx_pdu_region_id ON pdu(probation_region_id);
CREATE INDEX IF NOT EXISTS idx_pdu_name ON pdu(pdu_name);

CREATE INDEX IF NOT EXISTS idx_office_pdu_id ON office(pdu_id);
CREATE INDEX IF NOT EXISTS idx_office_region_id ON office(probation_region_id);
CREATE INDEX IF NOT EXISTS idx_office_name ON office(name);
