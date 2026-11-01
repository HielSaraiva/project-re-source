ALTER TABLE donations ADD COLUMN edit_version bigint NOT NULL DEFAULT 0;
ALTER TABLE donations ADD COLUMN deleted_at timestamptz;

-- A successful creation keeps its identity even after the inventory is deleted.
-- The same request cannot recreate stock, and retries never create a second donation.
CREATE TABLE donation_creation_requests (
    donor_user_id integer NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    request_key uuid NOT NULL,
    request_fingerprint varchar(64) NOT NULL,
    donation_id integer REFERENCES donations(id) ON DELETE SET NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (donor_user_id, request_key)
);
