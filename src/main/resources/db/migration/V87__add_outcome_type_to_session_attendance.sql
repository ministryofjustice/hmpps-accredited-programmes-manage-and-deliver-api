ALTER TABLE session_attendance ADD COLUMN outcome_type_code VARCHAR(5);
ALTER TABLE session_attendance ADD CONSTRAINT fk_outcome_type_code FOREIGN KEY (outcome_type_code) REFERENCES session_attendance_outcome_type(code);
