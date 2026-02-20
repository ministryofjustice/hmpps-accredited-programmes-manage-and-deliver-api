-- Creates the data_import_record table for tracking imported entities from the legacy system
-- This table maps source system IDs to M&D entity IDs, enabling idempotent imports

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

