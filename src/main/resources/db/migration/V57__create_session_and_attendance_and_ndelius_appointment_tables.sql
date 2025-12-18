-- Creates tables for Session, Session Attendance, and NDelius Appointment

CREATE TABLE session
(
    id                         UUID        NOT NULL PRIMARY KEY,
    programme_group_id         UUID        NOT NULL,
    module_session_template_id UUID,
    sequence_number            INTEGER     NOT NULL,
    session_type               TEXT        NOT NULL,
    is_catchup                 BOOLEAN     NOT NULL DEFAULT FALSE,
    location_name              TEXT,
    starts_at                  TIMESTAMP   NOT NULL,
    ends_at                    TIMESTAMP   NOT NULL,
    created_at                 TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by_username        TEXT,
    CONSTRAINT fk_session_programme_group
        FOREIGN KEY (programme_group_id)
            REFERENCES programme_group (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_session_module_session_template
        FOREIGN KEY (module_session_template_id)
            REFERENCES module_session_template (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_session_programme_group_id ON session (programme_group_id);
CREATE INDEX idx_session_module_session_template_id ON session (module_session_template_id);

COMMENT ON TABLE session IS 'An instance of a scheduled session for a Programme Group.';
COMMENT ON COLUMN session.id IS 'Primary key.';
COMMENT ON COLUMN session.programme_group_id IS 'FK to programme_group.id';
COMMENT ON COLUMN session.module_session_template_id IS 'FK to module_session_template.id';
COMMENT ON COLUMN session.sequence_number IS 'Ordering within group, across all modules.';
COMMENT ON COLUMN session.session_type IS 'group | one-to-one (stored as enum string values).';
COMMENT ON COLUMN session.is_catchup IS 'True if ad-hoc makeup session.';
COMMENT ON COLUMN session.location_name IS 'Optional location name.';
COMMENT ON COLUMN session.starts_at IS 'Start date and time of the session.';
COMMENT ON COLUMN session.ends_at IS 'End date and time of the session.';
COMMENT ON COLUMN session.created_at IS 'Creation timestamp.';
COMMENT ON COLUMN session.created_by_username IS 'The username of the person that created the session.';


CREATE TABLE session_attendance
(
    id                          UUID        NOT NULL PRIMARY KEY,
    session_id                  UUID        NOT NULL,
    group_membership_id         UUID        NOT NULL,
    arrived_at                  TIMESTAMP,
    departed_at                 TIMESTAMP,
    did_not_engage              BOOLEAN,
    legitimate_absence          BOOLEAN,
    notes                       TEXT,
    recorded_by_facilitator_id  UUID,
    recorded_at                 TIMESTAMP,
    CONSTRAINT fk_session_attendance_session
        FOREIGN KEY (session_id)
            REFERENCES session (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_session_attendance_group_membership
        FOREIGN KEY (group_membership_id)
            REFERENCES programme_group_membership (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_session_attendance_recorded_by_facilitator
        FOREIGN KEY (recorded_by_facilitator_id)
            REFERENCES facilitator (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_session_attendance_session_id ON session_attendance (session_id);
CREATE INDEX idx_session_attendance_group_membership_id ON session_attendance (group_membership_id);
CREATE INDEX idx_session_attendance_recorded_by_facilitator_id ON session_attendance (recorded_by_facilitator_id);

COMMENT ON TABLE session_attendance IS 'Attendance record for a particular session and group membership.';
COMMENT ON COLUMN session_attendance.arrived_at IS 'When an individual arrived at the session';
COMMENT ON COLUMN session_attendance.departed_at IS 'When an individual departed the session';
COMMENT ON COLUMN session_attendance.did_not_engage IS 'If an individual did not engage with the session';
COMMENT ON COLUMN session_attendance.legitimate_absence IS 'If an individual had a legitinate reason for non-attendance';
COMMENT ON COLUMN session_attendance.notes IS 'Session notes.';
COMMENT ON COLUMN session_attendance.recorded_by_facilitator_id IS 'The identifier or the facilitator recording the attendance.';
COMMENT ON COLUMN session_attendance.recorded_at IS 'The date and time when the attendance was recorded';


CREATE TABLE ndelius_appointment
(
    id                      UUID    NOT NULL PRIMARY KEY,
    session_attendance_id   UUID    NOT NULL UNIQUE,
    licence_condition_id    TEXT,
    requirement_id          TEXT,
    CONSTRAINT fk_ndelius_appointment_session_attendance
        FOREIGN KEY (session_attendance_id)
            REFERENCES session_attendance (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_ndelius_appointment_session_attendance_id ON ndelius_appointment (session_attendance_id);

COMMENT ON TABLE ndelius_appointment IS 'NDelius appointment data associated with session attendance.';
COMMENT ON COLUMN ndelius_appointment.licence_condition_id IS 'The unique licence condition identifier';
COMMENT ON COLUMN ndelius_appointment.requirement_id IS 'The unique requirement identifier';
