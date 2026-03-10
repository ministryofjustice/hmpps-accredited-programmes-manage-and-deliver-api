-- This migration file creates a new schema (im_data_import) relating to the
-- Interventions Manager data import process, as well as the data_import_record table,
-- for tracking/caching which entities have been imported from IM and into M&D.
--
-- The import process itself will be managed by the Interventions Manager Data Importer Service
-- (a separate service), but we are leaving management of the postgresql schemas with
-- the API codebase.  Running migrations in two repositories would be possible but
-- introduces too much chance for failures.
--
-- Lastly, this migration creates a table intended for static data (im_data_import.iaps_licreqnos)
-- which maps source referral IDs to IAPS and nDelius licence/requirement numbers.
-- this is a pragmatic choice, because new incoming Referrals will have their Requirement IDs
-- populated by our own web hooks and event listeners, but historic data from IM does not have
-- this data.  By using a static reference table (i.e. we do not expect to add data past
-- January 2026, we can pragmatically manage this process.
-- Requirement IDs are mandatory for integration with nDelius for creation of appointments,
-- and their outcomes.

CREATE TABLE data_import_record (
                                    id UUID PRIMARY KEY,
                                    entity_type VARCHAR(255) NOT NULL,
                                    source_id VARCHAR(255) NOT NULL,
                                    target_id UUID NOT NULL,
                                    imported_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    CONSTRAINT uk_data_import_record_entity_type_source_id UNIQUE (entity_type, source_id)
);

COMMENT ON TABLE data_import_record IS 'Records each successfully imported Interventions Manager entity and maps its source system (IM) ID to its M&D ID';
COMMENT ON COLUMN data_import_record.entity_type IS 'The type of entity imported, e.g. REFERRAL';
COMMENT ON COLUMN data_import_record.source_id IS 'The ID of the entity in Interventions Manager';
COMMENT ON COLUMN data_import_record.target_id IS 'The ID of the entity created in M&D';
COMMENT ON COLUMN data_import_record.imported_at IS 'Timestamp when the import occurred';

CREATE SCHEMA IF NOT EXISTS im_data_import;
COMMENT ON SCHEMA im_data_import IS 'Staging schema for Interventions Manager data import. Contains transformed IM data awaiting import into M&D tables.';

-- IAPS licence/requirement numbers table
CREATE TABLE im_data_import.iaps_licreqnos (
                                               id SERIAL PRIMARY KEY,
                                               source_referral_id VARCHAR(255) NOT NULL,
                                               licreqno VARCHAR(255) NOT NULL
);
COMMENT ON TABLE im_data_import.iaps_licreqnos IS 'Static data mapping source referrals to IAPS licence or requirement numbers';
COMMENT ON COLUMN im_data_import.iaps_licreqnos.source_referral_id IS 'Source referral ID (not unique - one referral may have multiple licreqnos)';
COMMENT ON COLUMN im_data_import.iaps_licreqnos.licreqno IS 'IAPS licence or requirement number';

-- Referral staging table
CREATE TABLE im_data_import.referral (
                                         source_referral_id VARCHAR(255) PRIMARY KEY,
                                         crn VARCHAR(255) NOT NULL,
                                         first_name VARCHAR(255) NOT NULL,
                                         last_name VARCHAR(255) NOT NULL,
                                         created_at DATE NOT NULL,
                                         sourced_from VARCHAR(255) NOT NULL,
                                         sourced_from_id VARCHAR(255) NOT NULL,
                                         sex VARCHAR(255) NOT NULL,
                                         date_of_birth DATE NOT NULL
);

COMMENT ON TABLE im_data_import.referral IS 'Staging table for referral data from the legacy system';
COMMENT ON COLUMN im_data_import.referral.source_referral_id IS 'The referral ID in the legacy system (primary key)';
COMMENT ON COLUMN im_data_import.referral.crn IS 'Case reference number';
COMMENT ON COLUMN im_data_import.referral.first_name IS 'First name of the person';
COMMENT ON COLUMN im_data_import.referral.last_name IS 'Last name of the person';
COMMENT ON COLUMN im_data_import.referral.created_at IS 'Date the referral was created in the legacy system';
COMMENT ON COLUMN im_data_import.referral.sourced_from IS 'Source type: REQUIREMENT or LICENCE_CONDITION';
COMMENT ON COLUMN im_data_import.referral.sourced_from_id IS 'ID of the source requirement or licence condition';
COMMENT ON COLUMN im_data_import.referral.sex IS 'Sex of the person';
COMMENT ON COLUMN im_data_import.referral.date_of_birth IS 'Date of birth of the person';

-- Reporting location staging table
CREATE TABLE im_data_import.reporting_location (
                                                   source_referral_id VARCHAR(255) PRIMARY KEY,
                                                   region_name VARCHAR(255) NOT NULL,
                                                   pdu_name VARCHAR(255) NOT NULL,
                                                   reporting_team_name VARCHAR(255) NOT NULL,
                                                   CONSTRAINT fk_reporting_location_referral FOREIGN KEY (source_referral_id)
                                                       REFERENCES im_data_import.referral (source_referral_id) ON DELETE CASCADE
);

COMMENT ON TABLE im_data_import.reporting_location IS 'Staging table for reporting location data associated with referrals';
COMMENT ON COLUMN im_data_import.reporting_location.source_referral_id IS 'Foreign key to im_data_import.referral';
COMMENT ON COLUMN im_data_import.reporting_location.region_name IS 'Name of the region';
COMMENT ON COLUMN im_data_import.reporting_location.pdu_name IS 'Name of the Probation Delivery Unit';
COMMENT ON COLUMN im_data_import.reporting_location.reporting_team_name IS 'Name of the reporting team';

