CREATE SEQUENCE match_protocol_number;
SELECT setval('match_protocol_number', COALESCE((SELECT max(split_part(protocol, '-', 3)::bigint) FROM matches), 0) + 1, false);

CREATE TABLE match_event_outbox (
    id uuid PRIMARY KEY,
    match_id integer NOT NULL REFERENCES matches(id) ON DELETE RESTRICT,
    protocol varchar(40) NOT NULL,
    event_type varchar(40) NOT NULL,
    donor_id integer NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    institution_id integer NOT NULL REFERENCES institutions(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    attempts integer NOT NULL DEFAULT 0,
    next_attempt_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_match_outbox_pending ON match_event_outbox(next_attempt_at, created_at) WHERE published_at IS NULL;

CREATE TABLE match_event_notifications (
    event_id uuid PRIMARY KEY REFERENCES match_event_outbox(id) ON DELETE RESTRICT,
    match_id integer NOT NULL REFERENCES matches(id) ON DELETE RESTRICT,
    event_type varchar(40) NOT NULL,
    donor_id integer NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    institution_id integer NOT NULL REFERENCES institutions(id) ON DELETE RESTRICT,
    processed_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_match_notifications_donor ON match_event_notifications(donor_id, processed_at);
CREATE INDEX idx_match_notifications_institution ON match_event_notifications(institution_id, processed_at);
