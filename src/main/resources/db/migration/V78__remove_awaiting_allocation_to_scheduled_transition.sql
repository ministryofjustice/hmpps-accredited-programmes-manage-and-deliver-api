DELETE FROM referral_status_transition
WHERE from_status = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting allocation')
  AND to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Scheduled');
