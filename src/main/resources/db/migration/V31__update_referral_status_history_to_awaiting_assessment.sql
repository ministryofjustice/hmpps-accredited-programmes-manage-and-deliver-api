UPDATE referral_status_history
SET referral_status_description_id = (SELECT id FROM referral_status_description WHERE description_text = 'Awaiting assessment');