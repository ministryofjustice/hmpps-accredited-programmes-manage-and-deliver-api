CREATE TABLE facilitator
(
    id                  UUID NOT NULL PRIMARY KEY,
    person_forename     TEXT NOT NULL,
    person_middle_name  TEXT NULL,
    person_surname      TEXT NOT NULL,
    ndelius_person_code TEXT NOT NULL,
    ndelius_team_code   TEXT NOT NULL,
    ndelius_team_name   TEXT NOT NULL
);

COMMENT ON TABLE facilitator IS 'Contains details about a facilitator for groups.';
COMMENT ON COLUMN facilitator.id IS 'Unique identifier for the facilitator';
COMMENT ON COLUMN facilitator.person_forename IS 'The forename of the facilitator';
COMMENT ON COLUMN facilitator.person_middle_name IS 'The middle name(s) of the facilitator';
COMMENT ON COLUMN facilitator.person_surname IS 'The surname of the facilitator';
COMMENT ON COLUMN facilitator.ndelius_person_code IS 'The person code fetched from Ndelius.';
COMMENT ON COLUMN facilitator.ndelius_team_code IS 'The code of the team the facilitator belongs to.';
COMMENT ON COLUMN facilitator.ndelius_team_name IS 'The name of the team the facilitator belongs to.';

CREATE INDEX idx_facilitator_ndelius_person_code ON facilitator (ndelius_person_code);
CREATE INDEX idx_facilitator_ndelius_tean_code ON facilitator (ndelius_team_code);

CREATE TABLE programme_group_facilitator
(
    id                 UUID      NOT NULL PRIMARY KEY,
    programme_group_id UUID      NOT NULL REFERENCES programme_group (id),
    facilitator_id     UUID      NOT NULL REFERENCES facilitator (id),
    added_at           TIMESTAMP NOT NULL,
    facilitator_type   TEXT      NOT NULL
);

COMMENT ON TABLE programme_group_facilitator IS 'Contains details around a programme group''s facilitators.';
COMMENT ON COLUMN programme_group_facilitator.id IS 'Unique identifier for the group facilitator.';
COMMENT ON COLUMN programme_group_facilitator.programme_group_id IS 'The programme group that this facilitator belongs to.';
COMMENT ON COLUMN programme_group_facilitator.facilitator_id IS 'References the ID of the facilitator.';
COMMENT ON COLUMN programme_group_facilitator.added_at IS 'Timestamp of when the facilitator was added to the group.';
COMMENT ON COLUMN programme_group_facilitator.facilitator_type IS 'The role that the facilitator has in the group.';

ALTER TABLE programme_group
    ADD COLUMN treatment_manager_id UUID NULL REFERENCES facilitator (id);

COMMENT ON COLUMN programme_group.treatment_manager_id IS 'References the unique ID of the treatment manager for this group.';