ALTER TABLE referral
ADD COLUMN is_seeded BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN referral.is_seeded IS 'Used for Referral Seeder Service, necessary to identify which Referrals can be torn-down';