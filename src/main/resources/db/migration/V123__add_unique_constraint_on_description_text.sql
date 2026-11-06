ALTER TABLE referral_status_description
    ADD CONSTRAINT referral_status_description_description_text_unique UNIQUE (description_text);
