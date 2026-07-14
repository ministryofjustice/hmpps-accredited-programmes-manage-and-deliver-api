ALTER TABLE message_history
ADD COLUMN ms_to_process BIGINT;

COMMENT ON COLUMN message_history.ms_to_process IS 'Time taken to process the message in milliseconds, null for all events processed before migration';
