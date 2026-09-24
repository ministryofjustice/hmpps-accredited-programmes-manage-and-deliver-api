UPDATE referral
SET created_by = 'SYSTEM'
WHERE created_by = 'Accredited Programmes automated update';

UPDATE referral_status_history
SET created_by = 'SYSTEM'
WHERE created_by = 'Accredited Programmes automated update';

UPDATE referral_cohort_history
SET created_by = 'SYSTEM'
WHERE created_by = 'Accredited Programmes automated update';

UPDATE referral_ldc_history
SET created_by = 'SYSTEM'
WHERE created_by = 'Accredited Programmes automated update';
