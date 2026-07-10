ALTER TABLE users DROP COLUMN IF EXISTS password;
ALTER TABLE users ADD COLUMN IF NOT EXISTS external_subject VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS users_external_subject_idx ON users (external_subject);
