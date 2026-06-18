USE gig_assessment;

ALTER TABLE users
  ADD COLUMN date_of_birth DATE NULL AFTER username;

-- Backfill existing users before making the column required.
-- Replace this placeholder date with each user's real date of birth.
-- UPDATE users
-- SET date_of_birth = '2000-01-01'
-- WHERE date_of_birth IS NULL;

-- Run this after every existing user has a date_of_birth.
-- ALTER TABLE users
--   MODIFY date_of_birth DATE NOT NULL;
