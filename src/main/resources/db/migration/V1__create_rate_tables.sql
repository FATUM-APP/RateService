CREATE TABLE order_snapshots (
    order_id       VARCHAR(36) NOT NULL PRIMARY KEY,
    service_id     VARCHAR(36),
    is_completed   BOOLEAN NOT NULL DEFAULT FALSE,
    version        BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE ratings (
    id             VARCHAR(36) NOT NULL PRIMARY KEY,
    order_id       VARCHAR(36) NOT NULL,
    target_type    VARCHAR(20) NOT NULL,
    target_id      VARCHAR(36) NOT NULL,
    rater_id       VARCHAR(36) NOT NULL,
    score          INTEGER NOT NULL,
    details        JSONB,
    created_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ratings_order_target UNIQUE (order_id, target_type),
    CONSTRAINT ck_ratings_score CHECK (score BETWEEN 1 AND 5)
);

CREATE INDEX ix_ratings_target ON ratings (target_type, target_id);
CREATE INDEX ix_ratings_order ON ratings (order_id);
