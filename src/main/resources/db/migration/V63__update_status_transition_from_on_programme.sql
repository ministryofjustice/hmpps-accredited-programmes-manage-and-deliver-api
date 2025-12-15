--remove transition from on-programme to Breach (non-attendance)
DELETE FROM referral_status_transition
WHERE from_status =
      (SELECT id FROM referral_status_description
       WHERE referral_status_description.description_text = 'On programme')
  AND to_status =
      (SELECT id FROM referral_status_description
       WHERE referral_status_description.description_text = 'Breach (non-attendance)');

--remove transition from on-programme to Programme complete
DELETE FROM referral_status_transition
WHERE from_status =
      (SELECT id FROM referral_status_description
       WHERE referral_status_description.description_text = 'On programme')
  AND to_status =
      (SELECT id FROM referral_status_description
       WHERE referral_status_description.description_text = 'Programme complete');

--insert transition on-programme to Deprioritised
INSERT INTO referral_status_transition (id, from_status, to_status, description, created_at, updated_at, deleted_at, is_continuing)
VALUES ('e30062f5-7c40-4b0e-8833-51d35bed12a7',
        (SELECT id FROM referral_status_description WHERE description_text = 'On programme'),
        (SELECT id FROM referral_status_description WHERE description_text = 'Deprioritised'),
        'The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are reprioritised.',
        NOW(),
        NULL,
        NULL,
        TRUE
       );