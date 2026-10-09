ALTER TABLE match_status_history
  ADD COLUMN event_type varchar(40) NOT NULL DEFAULT 'status_changed',
  ADD COLUMN event_title varchar(200),
  ADD COLUMN actor_name varchar(200),
  DROP CONSTRAINT ck_match_status_history_transition,
  ADD CONSTRAINT ck_match_status_history_transition CHECK (
    previous_status IS NULL OR previous_status <> new_status
      OR event_type = 'delivery_method_selected'
  ),
  ADD CONSTRAINT ck_match_status_history_event_type CHECK (
    event_type IN (
      'status_changed', 'proposal_submitted', 'proposal_accepted',
      'proposal_rejected', 'delivery_method_selected', 'delivery_reported',
      'shipment_reported', 'receipt_confirmed', 'donation_cancelled'
    )
  ),
  ADD CONSTRAINT ck_match_status_history_event_title CHECK (btrim(event_title) <> ''),
  ADD CONSTRAINT ck_match_status_history_actor_name CHECK (btrim(actor_name) <> '');

UPDATE match_status_history AS history
SET actor_name = COALESCE(
  (SELECT full_name FROM users WHERE id = history.changed_by_user_id),
  (SELECT legal_name FROM institutions WHERE id = history.changed_by_institution_id),
  'Sistema'
);
