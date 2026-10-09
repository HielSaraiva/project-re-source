ALTER TABLE match_event_outbox
    ADD COLUMN claim_token uuid,
    ADD COLUMN claimed_until timestamptz,
    ADD CONSTRAINT ck_match_outbox_claim CHECK (
        (claim_token IS NULL AND claimed_until IS NULL)
        OR (claim_token IS NOT NULL AND claimed_until IS NOT NULL)
    );

CREATE INDEX idx_match_outbox_claimable
    ON match_event_outbox(next_attempt_at, claimed_until, created_at)
    WHERE published_at IS NULL;

DROP INDEX idx_match_outbox_pending;
