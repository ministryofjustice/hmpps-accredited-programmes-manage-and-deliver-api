CREATE TABLE facilitator
(
    id                  UUID NOT NULL PRIMARY KEY,
    person_name         TEXT NOT NULL,
    ndelius_person_code TEXT NOT NULL,
    ndelius_team_code   TEXT NOT NULL,
    ndelius_team_name   TEXT NOT NULL
);

COMMENT ON TABLE facilitator IS 'Contains details about a facilitator for groups.';
COMMENT ON COLUMN facilitator.id IS 'Unique identifier for the facilitator';
COMMENT ON COLUMN facilitator.person_name IS 'The name of the facilitator';
COMMENT ON COLUMN facilitator.ndelius_person_code IS 'The person code fetched from Ndelius.';
COMMENT ON COLUMN facilitator.ndelius_team_code IS 'The code of the team the facilitator belongs to.';
COMMENT ON COLUMN facilitator.ndelius_team_name IS 'The name of the team the facilitator belongs to.';
