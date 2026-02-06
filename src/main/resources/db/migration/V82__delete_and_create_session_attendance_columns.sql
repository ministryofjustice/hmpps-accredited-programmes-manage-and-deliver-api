ALTER TABLE session_attendance ADD COLUMN attended BOOLEAN;
ALTER TABLE session_attendance DROP COLUMN arrived_at;
ALTER TABLE session_attendance DROP COLUMN departed_at;
