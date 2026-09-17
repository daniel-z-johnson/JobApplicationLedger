CREATE TABLE companies (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    company_type VARCHAR(127) NOT NULL,
    website_url TEXT,
    careers_url TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_companies_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT uq_companies_user_id
        UNIQUE (user_id, id),

    CONSTRAINT chk_companies_name_not_blank
        CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_companies_user_normalized_name
    ON companies (user_id, lower(btrim(name)));
