ALTER TABLE referral
    ADD COLUMN created_by TEXT DEFAULT 'Accredited Programmes automated update';

UPDATE referral set created_by = 'Accredited Programmes automated update' where created_by is null;
