CREATE TABLE email_verification_tokens (

    id BIGSERIAL PRIMARY KEY,

    uuid UUID NOT NULL UNIQUE,

    account_id BIGINT NOT NULL,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

    verified_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_email_verification_tokens_account
        FOREIGN KEY (account_id)
        REFERENCES users (id)
);

CREATE INDEX idx_email_verification_tokens_account_id
    ON email_verification_tokens(account_id);

CREATE INDEX idx_email_verification_tokens_expires_at
    ON email_verification_tokens(expires_at);