-- Awaiting allocation
UPDATE referral_status_description
SET label_colour = 'teal'
WHERE id = 'bb1e8c72-cf52-4297-94a4-3745c2a25178';

-- Breach
UPDATE referral_status_description
SET label_colour = 'yellow'
WHERE id = 'afc0b94c-b983-4a68-a109-0be29a7d3b2f';

-- Recall
UPDATE referral_status_description
SET label_colour = 'yellow'
WHERE id = 'aec91cd3-fba0-40a4-a5c6-7578b596af75';

-- Return to Court
UPDATE referral_status_description
SET label_colour = 'yellow'
WHERE id = 'e9fb9e3a-147b-4f26-aa0c-d852db4b7fef';

-- Deferred
UPDATE referral_status_description
SET label_colour = 'orange'
WHERE id = '69cfa202-47b6-4ae6-8175-69291b6acaaa';

-- Deprioritised
UPDATE referral_status_description
SET label_colour = 'orange'
WHERE id = 'bc8c7024-045b-4a82-bb97-e6b8c0f198cb';

-- Suitable but not ready
UPDATE referral_status_description
SET label_colour = 'orange'
WHERE id = '336b59cd-b467-4305-8547-6a645a8a3f91';
