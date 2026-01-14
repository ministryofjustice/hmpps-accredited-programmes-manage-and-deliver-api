ALTER TABLE ndelius_appointment
    DROP COLUMN session_attendance_id,
    DROP COLUMN licence_condition_id,
    DROP COLUMN requirement_id,
    ADD COLUMN ndelius_appointment_id UUID NOT NULL,
    ADD COLUMN session_id             UUID NOT NULL REFERENCES session (id),
    ADD COLUMN referral_id            UUID NOT NULL REFERENCES referral (id);

COMMENT ON COLUMN ndelius_appointment.ndelius_appointment_id IS 'The unique reference of the created appointment in Ndelius. This is passed to Ndelius on appointment creation';
COMMENT ON COLUMN ndelius_appointment.session_id IS 'The reference to the session that this appointment is for';

