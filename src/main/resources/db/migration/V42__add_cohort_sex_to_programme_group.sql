ALTER TABLE programme_group
    ADD COLUMN cohort TEXT    NOT NULL DEFAULT 'GENERAL_OFFENCE',
    ADD COLUMN sex    TEXT    NOT NULL DEFAULT 'MALE',
    ADD COLUMN is_ldc BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN programme_group.cohort IS 'The Cohort that the group will run for.';
COMMENT ON COLUMN programme_group.sex IS 'The Sex that the group will run for.';
COMMENT ON COLUMN programme_group.is_ldc IS 'Is the group for LDC referrals.';

CREATE INDEX idx_programme_group_cohort ON programme_group (cohort);
CREATE INDEX idx_programme_group_sex ON programme_group (sex);
CREATE INDEX idx_programme_group_isLdc ON programme_group (is_ldc);