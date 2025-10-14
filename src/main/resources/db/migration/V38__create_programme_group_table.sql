CREATE TABLE programme_group (
                         id UUID NOT NULL PRIMARY KEY,
                         code TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         created_by_username TEXT NOT NULL,
                         updated_at TIMESTAMP ,
                         updated_by_username TEXT,
                         deleted_at TIMESTAMP,
                         deleted_by_username TEXT
);

CREATE INDEX idx_programme_group_code ON programme_group (code);

COMMENT ON TABLE programme_group IS 'contains information about a group';
COMMENT ON COLUMN programme_group.id IS 'Unique identifier for a referral''s delivery location preferences';
COMMENT ON COLUMN programme_group.code IS 'The code used to identify the group';
COMMENT ON COLUMN programme_group.created_at IS 'Timestamp of when the group was created';
COMMENT ON COLUMN programme_group.created_by_username IS 'The username of the person that created the group';
COMMENT ON COLUMN programme_group.updated_at IS 'Timestamp of when the group was updated';
COMMENT ON COLUMN programme_group.updated_by_username IS 'The username of the person that updated the group';
COMMENT ON COLUMN programme_group.deleted_at IS 'Timestamp of when the group was deleted';
COMMENT ON COLUMN programme_group.deleted_by_username IS 'The username of the person that deleted the group';
