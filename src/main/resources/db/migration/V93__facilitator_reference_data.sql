-- Reference table mapping facilitators across IM, M&D, and nDelius
-- Necessary for the importing of ProgrammeGroups, for the creation of
-- appointment in nDelius
CREATE TABLE im_data_import.facilitator_resource_reference
(
    resource_id      TEXT    NOT NULL,
    forename         TEXT    NOT NULL,
    surname          TEXT    NOT NULL,
    email            TEXT,
    staff_code       TEXT,
    manually_updated BOOLEAN NOT NULL DEFAULT FALSE,
    comments         TEXT
);

CREATE UNIQUE INDEX uq_facilitator_resource_reference_resource_id ON im_data_import.facilitator_resource_reference (resource_id);

COMMENT ON TABLE im_data_import.facilitator_resource_reference
    IS 'Reference table mapping facilitators across IM, M&D, and nDelius.';

COMMENT ON COLUMN im_data_import.facilitator_resource_reference.resource_id
    IS 'IM identifier for Resource entity (e.g. Staff); primary means of identifying a facilitator across systems.';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.forename
    IS 'Facilitator forename.  E.g. Alice';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.surname
    IS 'Facilitator surname.  E.g. Smith';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.email
    IS 'Sourced from nDelius staff export.  May be absent due to incomplete match, or incomplete nDelius data';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.staff_code
    IS 'nDelius Code for the staff member.  May be absent due to incomplete nDelius data';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.manually_updated
    IS 'TRUE if this row has been manually curated (by a M&D developer) rather than derived from source data.';
COMMENT ON COLUMN im_data_import.facilitator_resource_reference.comments
    IS 'Explains the reason for any manual curation; expected to be populated when manually_updated is TRUE.';


