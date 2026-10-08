-- Drop dependent views so we can drop person_name from the referral table
DROP MATERIALIZED VIEW IF EXISTS referral_caselist_item_view;
DROP MATERIALIZED VIEW IF EXISTS group_waitlist_item_view;

ALTER TABLE referral DROP COLUMN person_name;

CREATE INDEX IF NOT EXISTS idx_referral_person_surname ON referral(person_surname);
CREATE INDEX IF NOT EXISTS idx_referral_person_forename ON referral(person_forename);
