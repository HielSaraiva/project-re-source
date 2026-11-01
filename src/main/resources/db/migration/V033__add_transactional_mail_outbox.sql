CREATE TABLE transactional_mail_outbox (
    id uuid PRIMARY KEY,
    event_key varchar(160) NOT NULL UNIQUE,
    notification_id uuid UNIQUE REFERENCES user_notifications(id) ON DELETE CASCADE,
    user_id integer REFERENCES users(id) ON DELETE CASCADE,
    institution_id integer REFERENCES institutions(id) ON DELETE CASCADE,
    title varchar(160) NOT NULL,
    message text NOT NULL,
    target_path varchar(240) NOT NULL,
    action_label varchar(80) NOT NULL,
    security_email boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    email_sent_at timestamptz,
    email_attempts integer NOT NULL DEFAULT 0,
    email_next_attempt_at timestamptz NOT NULL DEFAULT now(),
    email_claim uuid,
    email_claim_until timestamptz,
    CHECK ((user_id IS NULL) <> (institution_id IS NULL))
);
CREATE INDEX idx_transactional_mail_pending ON transactional_mail_outbox(email_next_attempt_at)
    WHERE email_sent_at IS NULL;

INSERT INTO transactional_mail_outbox
    (id, event_key, notification_id, user_id, institution_id, title, message, target_path,
     action_label, created_at, email_attempts, email_next_attempt_at, email_claim, email_claim_until)
SELECT id, 'notification:' || id, id, user_id, institution_id, title, message, target_path,
    'Acompanhar doação', created_at, email_attempts, email_next_attempt_at, email_claim, email_claim_until
FROM user_notifications WHERE email_ready AND email_sent_at IS NULL;
