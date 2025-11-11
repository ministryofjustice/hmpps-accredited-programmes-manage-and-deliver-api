ALTER TABLE referral_motivation_background_and_non_associations
    ALTER COLUMN maintains_innocence DROP NOT NULL,
    ALTER COLUMN motivations DROP NOT NULL,
    ALTER COLUMN non_associations DROP NOT NULL,
    ALTER COLUMN other_considerations DROP NOT NULL;
