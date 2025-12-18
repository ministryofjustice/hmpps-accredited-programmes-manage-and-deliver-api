-- Creates tables for Accredited Programme Templates, their Modules, and Module Session Templates

CREATE TABLE accredited_programme_template
(
    id           UUID    NOT NULL PRIMARY KEY,
    name         TEXT    NOT NULL,
    valid_from   DATE    NOT NULL,
    valid_until  DATE
);

COMMENT ON TABLE accredited_programme_template IS 'Template for structured curriculum delivered to Groups.';
COMMENT ON COLUMN accredited_programme_template.name IS 'Display name of the accredited programme template.';
COMMENT ON COLUMN accredited_programme_template.valid_from IS 'Date the template becomes valid.';
COMMENT ON COLUMN accredited_programme_template.valid_until IS 'Date the template is no longer valid (nullable).';


CREATE TABLE module
(
    id                                  UUID    NOT NULL PRIMARY KEY,
    accredited_programme_template_id    UUID    NOT NULL,
    name                                TEXT    NOT NULL,
    module_number                       INTEGER NOT NULL,
    CONSTRAINT fk_module_accredited_programme_template
        FOREIGN KEY (accredited_programme_template_id)
            REFERENCES accredited_programme_template (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_module_accredited_programme_template_id
    ON module (accredited_programme_template_id);

COMMENT ON TABLE module IS 'A group of cohesive, or thematically-related Sessions that a Person on Probation will need to complete as part of the Programme.';
COMMENT ON COLUMN module.accredited_programme_template_id IS 'FK to accredited_programme_template.id';
COMMENT ON COLUMN module.name IS 'The module display name.';
COMMENT ON COLUMN module.module_number IS 'Sequential number of the module within the programme.';


CREATE TABLE module_session_template
(
    id                UUID    NOT NULL PRIMARY KEY,
    module_id         UUID    NOT NULL,
    session_number    INTEGER NOT NULL,
    session_type      TEXT    NOT NULL,
    pathway           TEXT    NOT NULL,
    name              TEXT    NOT NULL,
    description       TEXT,
    duration_minutes  INTEGER NOT NULL,
    CONSTRAINT fk_module_session_template_module
        FOREIGN KEY (module_id)
            REFERENCES module (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_module_session_template_module_id
    ON module_session_template (module_id);

COMMENT ON TABLE module_session_template IS 'An individual teaching event that takes place at in a place at a given time.';
COMMENT ON COLUMN module_session_template.id IS 'Primary key.';
COMMENT ON COLUMN module_session_template.module_id IS 'FK to module.id';
COMMENT ON COLUMN module_session_template.session_number IS 'Sequential number of the session within the module.';
COMMENT ON COLUMN module_session_template.session_type IS 'The session session type (e.g., Group, 1-to-1).';
COMMENT ON COLUMN module_session_template.name IS 'Session display name.';
COMMENT ON COLUMN module_session_template.description IS 'Session description (optional).';
COMMENT ON COLUMN module_session_template.duration_minutes IS 'Scheduled duration of the session in minutes.';

-- Add one-to-one link from programme_group to accredited_programme_template
ALTER TABLE programme_group
    ADD COLUMN IF NOT EXISTS accredited_programme_template_id UUID;

ALTER TABLE programme_group
    ADD CONSTRAINT fk_programme_group_accredited_programme_template
        FOREIGN KEY (accredited_programme_template_id)
            REFERENCES accredited_programme_template (id)
            ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_programme_group_accredited_programme_template_id
    ON programme_group (accredited_programme_template_id);

COMMENT ON COLUMN programme_group.accredited_programme_template_id IS 'One-to-one FK to accredited_programme_template.id for the group''s curriculum template (nullable).';
