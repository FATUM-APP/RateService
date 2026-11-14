-- The copy of a user: identifier, name, role and the running average.
-- The provisional 3 is NOT stored: it is what an empty average answers, so the first real rating
-- replaces it by itself and it can never contaminate the sum.
CREATE TABLE user_snapshots (
    user_id        VARCHAR(36)  NOT NULL PRIMARY KEY,
    name           VARCHAR(142),
    role           VARCHAR(20)  NOT NULL DEFAULT 'CLIENT',
    total_score    BIGINT       NOT NULL DEFAULT 0,
    ratings_count  INTEGER      NOT NULL DEFAULT 0,
    version        BIGINT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_user_snapshots_role ON user_snapshots (role);
