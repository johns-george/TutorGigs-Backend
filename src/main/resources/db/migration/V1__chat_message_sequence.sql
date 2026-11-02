-- Migration: Add per-chat sequence numbers to message and chats
ALTER TABLE IF EXISTS chats ADD COLUMN IF NOT EXISTS last_sequence_number BIGINT DEFAULT 0;
ALTER TABLE IF EXISTS message ADD COLUMN IF NOT EXISTS sequence_number BIGINT;

CREATE INDEX IF NOT EXISTS idx_message_chat_seq ON message(chat_id, sequence_number);

-- Backfill sequence numbers for any existing messages per chat
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'message') THEN
        UPDATE message m
        SET sequence_number = sub.rn
        FROM (
            SELECT id, ROW_NUMBER() OVER (PARTITION BY chat_id ORDER BY timestamp ASC, id ASC) AS rn
            FROM message
        ) sub
        WHERE m.id = sub.id AND m.sequence_number IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'chats') THEN
        UPDATE chats c
        SET last_sequence_number = COALESCE((
            SELECT MAX(sequence_number) FROM message WHERE message.chat_id = c.id
        ), 0)
        WHERE c.last_sequence_number IS NULL OR c.last_sequence_number = 0;
    END IF;
END $$;
