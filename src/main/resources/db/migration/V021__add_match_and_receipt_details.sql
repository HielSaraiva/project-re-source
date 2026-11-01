ALTER TABLE matches
  ADD COLUMN proposal_message text,
  ADD COLUMN accepted_at timestamptz,
  ADD COLUMN cancelled_at timestamptz,
  ADD COLUMN completed_at timestamptz;

ALTER TABLE deliveries
  ADD COLUMN method_confirmed_at timestamptz,
  ADD COLUMN received_by_name varchar(200),
  ADD COLUMN delivery_notes text,
  ADD COLUMN reported_at timestamptz,
  ADD COLUMN receipt_confirmed_at timestamptz,
  ADD COLUMN receipt_confirmed_by_institution_id integer,
  ADD CONSTRAINT fk_deliveries_receipt_institution
    FOREIGN KEY (receipt_confirmed_by_institution_id) REFERENCES institutions (id) ON DELETE RESTRICT;

ALTER TABLE donation_status_history
  ADD COLUMN changed_by_institution_id integer,
  ADD CONSTRAINT fk_donation_status_history_institution
    FOREIGN KEY (changed_by_institution_id) REFERENCES institutions (id) ON DELETE RESTRICT,
  ADD CONSTRAINT ck_donation_status_history_single_actor
    CHECK (changed_by_user_id IS NULL OR changed_by_institution_id IS NULL);

ALTER TABLE match_status_history
  ADD COLUMN changed_by_institution_id integer,
  ADD CONSTRAINT fk_match_status_history_institution
    FOREIGN KEY (changed_by_institution_id) REFERENCES institutions (id) ON DELETE RESTRICT,
  ADD CONSTRAINT ck_match_status_history_single_actor
    CHECK (changed_by_user_id IS NULL OR changed_by_institution_id IS NULL);
    