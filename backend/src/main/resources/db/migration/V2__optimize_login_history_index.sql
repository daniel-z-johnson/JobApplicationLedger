DROP INDEX idx_logins_user_id;

CREATE INDEX idx_logins_user_login_at
    ON logins (user_id, login_at DESC);
