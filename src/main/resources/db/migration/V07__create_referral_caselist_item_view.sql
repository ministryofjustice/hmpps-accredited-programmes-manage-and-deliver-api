-- Removing table as approach has changed to use a view rather than this .
DROP TABLE referral_caselist_item;

CREATE MATERIALIZED VIEW referral_caselist_item_view AS
SELECT r.crn,
       r.person_name,
       rsh.status
FROM referral r
         JOIN referral_status_history_mapping rshm ON r.id = rshm.referral_id
         JOIN referral_status_history rsh ON rshm.referral_status_history_id = rsh.id
WHERE rsh.created_at = (SELECT rsh2.start_date
                        FROM referral_status_history_mapping rshm2
                                 JOIN referral_status_history rsh2 ON rshm2.referral_status_history_id = rsh2.id
                        WHERE rshm2.referral_id = r.id
                        ORDER BY start_date DESC
                        LIMIT 1);

-- Need unique index to be able to refresh view.
CREATE UNIQUE INDEX idx_referral_crn_person_name_status ON referral_caselist_item_view (crn, person_name, status);
CREATE INDEX idx_referral_crn ON referral_caselist_item_view (crn);
CREATE INDEX idx_referral_person_name ON referral_caselist_item_view (person_name);
CREATE INDEX idx_referral_status ON referral_caselist_item_view (status);

CREATE OR REPLACE FUNCTION refresh_caselist_item_view()
    RETURNS TRIGGER AS
$$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY referral_caselist_item_view;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_refresh_referral_status
    AFTER INSERT OR UPDATE OR DELETE
    ON referral
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE TRIGGER trigger_refresh_referral_status_history
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_history
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

CREATE TRIGGER trigger_refresh_referral_status_mapping
    AFTER INSERT OR UPDATE OR DELETE
    ON referral_status_history_mapping
    FOR EACH STATEMENT
EXECUTE FUNCTION refresh_caselist_item_view();

