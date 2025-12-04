ALTER TABLE programme_group_membership
    ADD CONSTRAINT referral_id_fk FOREIGN KEY (referral_id) REFERENCES referral (id);