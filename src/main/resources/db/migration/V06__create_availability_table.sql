CREATE TABLE availability
(
    id                UUID NOT NULL PRIMARY KEY,
    referral_id       UUID NOT NULL,
    start_date        TIMESTAMP NOT NULL,
    end_date          TIMESTAMP,
    other_details     TEXT,
    last_modified_by  VARCHAR(50) NOT NULL,
    last_modified_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_referral FOREIGN KEY (referral_id) REFERENCES referral (id)
);


CREATE TABLE slot
(
    id              UUID NOT NULL PRIMARY KEY,
    day_of_week     VARCHAR(20) NOT NULL,
    slot_name       VARCHAR(20) NOT NULL,
    availability_id UUID NOT NULL,
    CONSTRAINT fk_availability FOREIGN KEY (availability_id) REFERENCES availability(id)
);