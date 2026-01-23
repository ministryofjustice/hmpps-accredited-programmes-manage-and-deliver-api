ALTER TABLE session_facilitator ADD COLUMN facilitator_type TEXT NOT NULL;
COMMENT ON COLUMN programme_group_facilitator.facilitator_type IS 'The role that the facilitator has in the group.';
