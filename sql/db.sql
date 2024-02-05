CREATE DATABASE rewiewboard;

\c rewiewboard;
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
    created timestamp NOT NULL DEFAULT now(),
    updated timestamp NOT NULL DEFAULT now()
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

