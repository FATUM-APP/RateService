-- The order carries its two parties, because the completion writes a rating for each of them.
ALTER TABLE order_snapshots ADD COLUMN client_id   VARCHAR(36);
ALTER TABLE order_snapshots ADD COLUMN provider_id VARCHAR(36);

-- A completed order is born with its three ratings at 5. This flag says which ones the user has
-- already replaced: false while it is still the initial 5, true once the user sent theirs.
ALTER TABLE ratings ADD COLUMN updated BOOLEAN NOT NULL DEFAULT FALSE;

-- The row is updated in place, so it needs to know when it changed.
ALTER TABLE ratings ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
