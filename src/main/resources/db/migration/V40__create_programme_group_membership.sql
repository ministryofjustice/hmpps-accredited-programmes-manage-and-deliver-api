CREATE TABLE programme_group_membership (
                         id UUID NOT NULL PRIMARY KEY,
                         programme_group_id UUID NOT NULL,
                         referral_id UUID NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         created_by_username TEXT NOT NULL,
                         deleted_at TIMESTAMP,
                         deleted_by_username TEXT
);

COMMENT ON TABLE programme_group_membership IS 'contains information about referrals allocated to a group';
COMMENT ON COLUMN programme_group_membership.id IS 'Unique identifier for a programme group membership';
COMMENT ON COLUMN programme_group_membership.programme_group_id IS 'Unique identifier of the programme group the referral is allocated to';
COMMENT ON COLUMN programme_group_membership.referral_id IS 'Unique identifier for a referral that is allocated to a programme group';
COMMENT ON COLUMN programme_group_membership.created_at IS 'Timestamp of when the group membership was created';
COMMENT ON COLUMN programme_group_membership.created_by_username IS 'The username of the person that created the group membership';
COMMENT ON COLUMN programme_group_membership.deleted_at IS 'Timestamp of when the referral was unallocated from the group';
COMMENT ON COLUMN programme_group_membership.deleted_by_username IS 'The username of the person that unallocated the referral from the group';
