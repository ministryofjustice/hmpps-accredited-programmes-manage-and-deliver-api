DROP VIEW IF EXISTS group_waitlist_item_view;

CREATE VIEW group_waitlist_item_view AS
SELECT r.id                                           as referral_id,
       r.crn,
       r.person_name,
       r.sentence_end_date,
       r.sourced_from,
       -- Default to GENERAL_OFFENCE if there are no entries in the referral_cohort_history_table
       COALESCE(lc.cohort, 'GENERAL_OFFENCE')         as cohort,
       -- Default to false if there are no entries in the referral_ldc_history_table
       COALESCE(lds.has_ldc, false)                   as has_ldc,
       r.date_of_birth,
       r.sex,
       ls.status,
       ls.status_colour,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.region_name, 'No information')    as region_name,
       COALESCE(rrl.pdu_name, 'No information')       as pdu_name,
       COALESCE(rrl.reporting_team, 'No information') as reporting_team,
       pgm.programme_group_id                         as active_programme_group_id

FROM referral r
JOIN LATERAL (
    SELECT rsd.description_text as status, rsd.label_colour as status_colour
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
LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id
LEFT JOIN LATERAL (
    SELECT programme_group_id
    FROM programme_group_membership
    WHERE referral_id = r.id AND deleted_at IS NULL
    ORDER BY created_at DESC
    LIMIT 1
) pgm ON TRUE
WHERE pgm.programme_group_id IS NOT NULL
   OR ls.status = 'Awaiting allocation';
