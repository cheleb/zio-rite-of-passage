CREATE TABLE IF NOT EXISTS invites(
    id bigserial PRIMARY KEY,
    user_name text NOT NULL,
    company_id bigint NOT NULL,
    n_invites int NOT NULL,
    active boolean NOT NULL DEFAULT FALSE
);

