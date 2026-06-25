-- Enforce uniqueness for referral rows created.
-- Use NULLS NOT DISTINCT so rows with NULL sourced_from are also treated as duplicates.
CREATE UNIQUE INDEX uq_referral_crn_event_id_sourced_from
	ON referral (crn, event_id, sourced_from) NULLS NOT DISTINCT;

