ALTER TABLE session_attendance
    DROP COLUMN group_membership_id,
    DROP COLUMN session_id,
    ADD COLUMN attendee_id UUID NOT NULL REFERENCES attendee (id);

-- ALTER TABLE ndelius_appointment
--     DROP COLUMN referral_id,
--     ADD COLUMN attendee_id UUID NOT NULL REFERENCES attendee (id);