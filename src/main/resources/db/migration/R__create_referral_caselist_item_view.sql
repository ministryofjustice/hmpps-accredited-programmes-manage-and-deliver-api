DROP VIEW IF EXISTS referral_caselist_item_view;

CREATE VIEW referral_caselist_item_view AS
SELECT r.id,
       r.crn,
       r.person_name,
       -- Default to GENERAL_OFFENCE if there are no entries in the referral_cohort_history_table
       COALESCE(lc.cohort, 'GENERAL_OFFENCE')                 as cohort,
       r.sentence_end_date,
       r.sourced_from                                         as sentence_end_date_source,
       ls.status,
       ls.status_label_colour,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false)                           as has_ldc,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.pdu_name, 'UNKNOWN_PDU_NAME')             as pdu_name,
       COALESCE(rrl.reporting_team, 'UNKNOWN_REPORTING_TEAM') as reporting_team,
       COALESCE(rrl.region_name, 'UNKNOWN_REGION_NAME')       as region_name

FROM referral r
JOIN LATERAL (
    SELECT rsd.description_text as status, rsd.label_colour as status_label_colour
    FROM referral_status_history rsh
    JOIN referral_status_description rsd ON rsh.referral_status_description_id = rsd.id
    WHERE rsh.referral_id = r.id
    ORDER BY rsh.created_at DESC
    LIMIT 1
) ls ON TRUE
LEFT JOIN LATERAL (
    SELECT has_ldc
    FROM referral_ldc_history
    WHERE referral_id = r.id
    ORDER BY created_at DESC
    LIMIT 1
) lds ON TRUE
LEFT JOIN LATERAL (
    SELECT cohort
    FROM referral_cohort_history
    WHERE referral_id = r.id
    ORDER BY created_at DESC
    LIMIT 1
) lc ON TRUE
LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id;
