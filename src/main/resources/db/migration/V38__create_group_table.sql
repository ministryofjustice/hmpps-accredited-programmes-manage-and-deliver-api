CREATE TABLE groups (
                         id UUID NOT NULL PRIMARY KEY,
                         code TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         created_by_username TEXT NOT NULL,
                         updated_at TIMESTAMP ,
                         updated_by_username TEXT,
                         deleted_at TIMESTAMP,
                         deleted_by_username TEXT
);

COMMENT ON TABLE groups IS 'contains information about a group';
COMMENT ON COLUMN groups.id IS 'Unique identifier for a referral''s delivery location preferences';
COMMENT ON COLUMN groups.code IS 'The code used to identify the group';
COMMENT ON COLUMN groups.created_at IS 'Timestamp of when the group was created';
COMMENT ON COLUMN groups.created_by_username IS 'The username of the person that created the group';
COMMENT ON COLUMN groups.updated_at IS 'Timestamp of when the group was updated';
COMMENT ON COLUMN groups.updated_by_username IS 'The username of the person that updated the group';
COMMENT ON COLUMN groups.deleted_at IS 'Timestamp of when the group was deleted';
COMMENT ON COLUMN groups.deleted_by_username IS 'The username of the person that deleted the group';
