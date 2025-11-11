ALTER TABLE programme_group
    ADD COLUMN region_name TEXT NOT NULL DEFAULT 'UNKNOW REGION';

COMMENT ON COLUMN programme_group.region_name IS 'The region name of the group. Determined by the logged in user on creation.';

CREATE INDEX idx_programme_group_region_name ON programme_group (region_name);