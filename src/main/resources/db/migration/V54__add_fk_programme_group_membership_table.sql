ALTER TABLE programme_group_membership
    ADD CONSTRAINT programme_group_id_fk FOREIGN KEY (programme_group_id) REFERENCES programme_group (id);