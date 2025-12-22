ALTER TABLE ndelius_appointment
    DROP CONSTRAINT ndelius_appointment_session_attendance_id_key,
    ADD COLUMN ndelius_appointment_id UUID NOT NULL;

COMMENT ON COLUMN ndelius_appointment.ndelius_appointment_id IS 'The unique reference of the created appointment in Ndelius. This is passed to Ndelius on appointment creation';