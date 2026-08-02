CREATE TABLE users (
    id         UUID PRIMARY KEY,
    email      TEXT        NOT NULL UNIQUE,
    username   TEXT        NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE logins (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address TEXT        NOT NULL,
    login_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_logins_user_id ON logins (user_id);