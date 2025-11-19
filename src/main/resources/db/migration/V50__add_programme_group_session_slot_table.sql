CREATE TABLE programme_group_session_slot (
            id                       UUID PRIMARY KEY NOT NULL,
            programme_group_id       UUID NOT NULL,
            day_of_week              TEXT NOT NULL,
            start_time               TIME NOT NULL,
            CONSTRAINT fk_programme_group_session_slot_programme_group_id FOREIGN KEY (programme_group_id) REFERENCES programme_group (id)
);
