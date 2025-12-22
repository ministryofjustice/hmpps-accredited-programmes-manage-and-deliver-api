-- Remove a person from a group when they reach programme complete
UPDATE referral_status_transition
SET is_continuing = FALSE
WHERE to_status = (SELECT id FROM referral_status_description WHERE description_text = 'Programme complete')