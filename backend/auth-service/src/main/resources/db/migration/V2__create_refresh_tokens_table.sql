CREATE TABLE refresh_tokens (

    id BIGSERIAL PRIMARY KEY,

    uuid UUID NOT NULL UNIQUE,

    account_id BIGINT NOT NULL,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMPTZ NOT NULL,

    revoked_at TIMESTAMPTZ,

    created_ip VARCHAR(45),

    user_agent VARCHAR(1000),

    replaced_by_token_id BIGINT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_refresh_tokens_account
        FOREIGN KEY (account_id)
        REFERENCES users(id),

    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id)
        REFERENCES refresh_tokens(id)
);

CREATE INDEX idx_refresh_tokens_account_id
    ON refresh_tokens(account_id);

CREATE INDEX idx_refresh_tokens_token_hash
    ON refresh_tokens(token_hash);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);