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

