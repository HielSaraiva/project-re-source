ALTER TABLE matches
  ADD COLUMN protocol varchar(40),
  ADD COLUMN rejected_at timestamptz,
  ADD COLUMN rejection_reason text,
  ADD COLUMN cancellation_reason text,
  ADD COLUMN expired_stage varchar(30);

UPDATE matches
SET protocol = 'INT-' || extract(year FROM created_at AT TIME ZONE 'America/Fortaleza')::integer || '-' || id;

ALTER TABLE matches
  ALTER COLUMN protocol SET NOT NULL,
  ADD CONSTRAINT uq_matches_protocol UNIQUE (protocol),
  ADD CONSTRAINT ck_matches_protocol CHECK (protocol ~ '^INT-[0-9]{4}-[0-9]+$'),
  ADD CONSTRAINT ck_matches_rejection_reason CHECK (btrim(rejection_reason) <> ''),
  ADD CONSTRAINT ck_matches_cancellation_reason CHECK (btrim(cancellation_reason) <> ''),
  ADD CONSTRAINT ck_matches_expired_stage CHECK (
    expired_stage IN ('acceptance', 'choice', 'in_person', 'carrier')
  );
