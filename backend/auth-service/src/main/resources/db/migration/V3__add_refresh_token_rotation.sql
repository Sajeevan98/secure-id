ALTER TABLE refresh_tokens
    ADD COLUMN family_id UUID,
    ADD COLUMN revocation_reason VARCHAR(30);

UPDATE refresh_tokens
SET family_id = uuid
WHERE family_id IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN family_id SET NOT NULL;

CREATE INDEX idx_refresh_tokens_family_id
    ON refresh_tokens(family_id);