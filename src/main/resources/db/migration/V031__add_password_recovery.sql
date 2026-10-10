CREATE TABLE password_reset_tokens (
    token_hash char(64) PRIMARY KEY,
    user_id integer REFERENCES users(id) ON DELETE CASCADE,
    institution_id integer REFERENCES institutions(id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    CONSTRAINT ck_reset_account CHECK ((user_id IS NOT NULL) <> (institution_id IS NOT NULL)),
    CONSTRAINT ck_reset_expiration CHECK (expires_at > created_at)
);
CREATE INDEX idx_reset_user ON password_reset_tokens(user_id, created_at);
CREATE INDEX idx_reset_institution ON password_reset_tokens(institution_id, created_at);
CREATE INDEX idx_reset_expiration ON password_reset_tokens(expires_at);

CREATE TABLE password_reset_rate_limits (
    client_hash char(64) PRIMARY KEY,
    window_started_at timestamptz NOT NULL,
    requests integer NOT NULL CHECK (requests > 0)
);
