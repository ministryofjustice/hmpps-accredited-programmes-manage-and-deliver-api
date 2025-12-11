-- This is currently a unique index ( V55) but should not be
DROP INDEX idx_programme_group_accredited_programme_template_id;
CREATE INDEX IF NOT EXISTS idx_programme_group_accredited_programme_template_id
    ON programme_group (accredited_programme_template_id);
