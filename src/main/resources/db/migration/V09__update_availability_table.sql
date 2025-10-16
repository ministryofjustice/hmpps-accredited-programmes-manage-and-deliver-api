
DROP TABLE IF EXISTS slot;
DROP TABLE IF EXISTS availability;

CREATE TABLE availability
(
    id                UUID NOT NULL PRIMARY KEY,
    referral_id       UUID NOT NULL,
    start_date        DATE NOT NULL,
    end_date          DATE,
    other_details     TEXT,
    last_modified_by  VARCHAR NOT NULL,
    last_modified_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_referral FOREIGN KEY (referral_id) REFERENCES referral (id)
);


CREATE TABLE availability_slot
(
    id              UUID NOT NULL PRIMARY KEY,
    day_of_week     VARCHAR(20) NOT NULL,
    slot_name       VARCHAR(20) NOT NULL,
    availability_id UUID NOT NULL,
    CONSTRAINT fk_availability FOREIGN KEY (availability_id) REFERENCES availability(id)
);