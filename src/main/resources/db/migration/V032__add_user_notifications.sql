CREATE TABLE user_notifications (
    id uuid PRIMARY KEY,
    event_id uuid NOT NULL REFERENCES match_event_outbox(id) ON DELETE CASCADE,
    user_id integer REFERENCES users(id) ON DELETE CASCADE,
    institution_id integer REFERENCES institutions(id) ON DELETE CASCADE,
    title varchar(160) NOT NULL,
    message varchar(1000) NOT NULL,
    target_path varchar(240) NOT NULL,
    created_at timestamptz NOT NULL,
    read_at timestamptz,
    email_ready boolean NOT NULL DEFAULT false,
    email_sent_at timestamptz,
    email_attempts integer NOT NULL DEFAULT 0,
    email_next_attempt_at timestamptz NOT NULL DEFAULT now(),
    email_claim uuid,
    email_claim_until timestamptz,
    CHECK ((user_id IS NULL) <> (institution_id IS NULL)),
    UNIQUE (event_id, user_id),
    UNIQUE (event_id, institution_id)
);
CREATE INDEX idx_notifications_user ON user_notifications(user_id, created_at DESC, id);
CREATE INDEX idx_notifications_institution ON user_notifications(institution_id, created_at DESC, id);
CREATE INDEX idx_notification_email_pending ON user_notifications(email_next_attempt_at)
    WHERE email_ready AND email_sent_at IS NULL;
