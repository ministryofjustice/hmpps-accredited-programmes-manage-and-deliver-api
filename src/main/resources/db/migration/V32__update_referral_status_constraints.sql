ALTER TABLE referral_status_history ALTER COLUMN created_by SET NOT NULL;

UPDATE referral_status_description SET label_colour = 'purple' where id ='76b2f8d8-260c-4766-a716-de9325292609';

ALTER TABLE referral_status_description ALTER COLUMN label_colour SET NOT NULL;
