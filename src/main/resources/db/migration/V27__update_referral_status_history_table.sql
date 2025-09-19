-- We have to drop these first, as they have dependencies
DROP MATERIALIZED VIEW referral_caselist_item_view;
DROP TABLE referral_status_history_mapping;

-- Then we have to add these columns to the table, one-by-one
ALTER TABLE referral_status_history
    ADD COLUMN referral_id UUID REFERENCES referral (id);

ALTER TABLE referral_status_history
    DROP COLUMN status;

-- Now we have to update the Materialised View to make sure of the new Referral -> ReferralStatusHistory
-- Simplified mapping
CREATE MATERIALIZED VIEW referral_caselist_item_view AS
WITH latest_status AS (
    SELECT referral_id,
           referral_status_description_id,
           ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY start_date DESC) as rn
    FROM referral_status_history
)
SELECT r.id,
       r.crn,
       r.person_name,
       r.cohort,
       rsd.description_text as status
FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id;

-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX idx_referral_id ON referral_caselist_item_view (id);
CREATE INDEX idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);

-- Finally, create some indexes on the new rows, for performance
CREATE INDEX idx_referral_status_history_referral_id ON referral_status_history (referral_id);

COMMENT ON COLUMN referral_status_history.referral_id IS 'References the Referral that this Status History belongs to';