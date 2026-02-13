CREATE TABLE session_notes_history
(
    id             UUID                        NOT NULL PRIMARY KEY,
    attendance_id  UUID                        NOT NULL,
    notes          TEXT,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by     VARCHAR(255)                NOT NULL,
    CONSTRAINT fk_session_notes_history_attendance FOREIGN KEY (attendance_id) REFERENCES session_attendance (id)
);

ALTER TABLE session_attendance DROP COLUMN notes;
