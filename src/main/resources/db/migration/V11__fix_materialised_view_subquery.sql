-- Need to drop to add new columns.
DROP MATERIALIZED VIEW referral_caselist_item_view;

CREATE MATERIALIZED VIEW referral_caselist_item_view AS
SELECT r.id,
       r.crn,
       r.person_name,
       rsh.status
FROM referral r
         JOIN referral_status_history_mapping rshm ON r.id = rshm.referral_id
         JOIN referral_status_history rsh ON rshm.referral_status_history_id = rsh.id
WHERE rsh.start_date = (SELECT rsh2.start_date
                        FROM referral_status_history_mapping rshm2
                                 JOIN referral_status_history rsh2 ON rshm2.referral_status_history_id = rsh2.id
                        WHERE rshm2.referral_id = r.id
                        ORDER BY start_date DESC
                        LIMIT 1);

-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX idx_referral_id ON referral_caselist_item_view (id);
CREATE INDEX idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);