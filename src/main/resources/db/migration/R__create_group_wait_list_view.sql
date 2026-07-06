DROP VIEW IF EXISTS group_waitlist_item_view;

CREATE VIEW group_waitlist_item_view AS
WITH latest_status AS (SELECT referral_id,
                              referral_status_description_id,
                              ROW_NUMBER()
                              OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       FROM referral_status_history),
     latest_ldc_status AS (SELECT referral_id,
                                  has_ldc,
                                  ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                           from referral_ldc_history),
     latest_cohort AS (SELECT referral_id,
                              cohort,
                              ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                       from referral_cohort_history),
     active_group_membership AS (SELECT referral_id,
                                        programme_group_id,
                                        ROW_NUMBER() OVER (PARTITION BY referral_id ORDER BY created_at DESC) as rn
                                 from programme_group_membership
                                 WHERE deleted_at IS null)

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
       rsd.description_text                           as status,
       rsd.label_colour                               as status_colour,
       -- Default values if there are no entries in the referral_reporting_location table for this referral yet
       COALESCE(rrl.region_name, 'No information')    as region_name,
       COALESCE(rrl.pdu_name, 'No information')       as pdu_name,
       COALESCE(rrl.reporting_team, 'No information') as reporting_team,
       pgm.programme_group_id                         as active_programme_group_id

FROM referral r
         JOIN latest_status ls ON r.id = ls.referral_id AND ls.rn = 1
         JOIN referral_status_description rsd ON ls.referral_status_description_id = rsd.id
         LEFT JOIN latest_ldc_status lds ON r.id = lds.referral_id and lds.rn = 1
         LEFT JOIN latest_cohort lc ON r.id = lc.referral_id and lc.rn = 1
         LEFT JOIN referral_reporting_location rrl on r.id = rrl.referral_id
         LEFT JOIN active_group_membership pgm on r.id = pgm.referral_id and ls.rn = 1
WHERE pgm IS NOT NULL
   OR rsd.description_text = 'Awaiting allocation';
