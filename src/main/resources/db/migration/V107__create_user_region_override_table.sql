CREATE TABLE user_region_override
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    username    TEXT                                        NOT NULL,
    region_name TEXT                                        NOT NULL,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    created_by  TEXT             DEFAULT current_user       NOT NULL,
    deleted_at  TIMESTAMP,
    deleted_by  TEXT
);

CREATE UNIQUE INDEX uq_user_region_override_username_region_active
    ON user_region_override (lower(username), region_name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_user_region_override_username_active
    ON user_region_override (lower(username))
    WHERE deleted_at IS NULL;

CREATE INDEX idx_user_region_override_region_name_active
    ON user_region_override (region_name)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE user_region_override IS 'Manual internal override mapping from username to region name for temporary access';
COMMENT ON COLUMN user_region_override.username IS 'HMPPS Auth username associated with the manual region override';
COMMENT ON COLUMN user_region_override.region_name IS 'Region name used for referral and programme group region filtering';
COMMENT ON COLUMN user_region_override.created_at IS 'Timestamp when the override was created';
COMMENT ON COLUMN user_region_override.created_by IS 'Database user that created the override';
COMMENT ON COLUMN user_region_override.deleted_at IS 'Soft delete timestamp; null means active';
COMMENT ON COLUMN user_region_override.deleted_by IS 'Username/system that soft deleted the override';