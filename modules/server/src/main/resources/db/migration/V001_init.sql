CREATE TABLE IF NOT EXISTS companies(
    id bigserial PRIMARY KEY,
    slug text NOT NULL UNIQUE,
    name text NOT NULL UNIQUE,
    url text NOT NULL,
    location text,
    country text,
    industry text,
    image text,
    tags text[]
);

CREATE TABLE IF NOT EXISTS reviews(
    id bigserial PRIMARY KEY,
    company_id bigint NOT NULL,
    user_id bigint NOT NULL,
    management int NOT NULL,
    culture int NOT NULL,
    salary int NOT NULL,
    benefits int NOT NULL,
    would_recommend int NOT NULL,
    review text NOT NULL,
    created timestamp with time zone NOT NULL DEFAULT now(),
    updated timestamp with time zone NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users(
    id bigserial PRIMARY KEY,
    email text NOT NULL UNIQUE,
    hashed_password text NOT NULL
);

CREATE TABLE IF NOT EXISTS recovery_tokens(
    email text NOT NULL PRIMARY KEY,
    token text NOT NULL,
    expiration bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS invites(
    id bigserial PRIMARY KEY,
    user_name text NOT NULL,
    company_id bigint NOT NULL,
    n_invites int NOT NULL,
    active boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS review_summaries(
    company_id bigint PRIMARY KEY,
    content text,
    created timestamp with time zone NOT NULL DEFAULT now()
);

