ALTER TABLE deliveries DROP CONSTRAINT ck_deliveries_tracking_code;
ALTER TABLE deliveries ADD CONSTRAINT ck_deliveries_tracking_code CHECK (
  method <> 'carrier'
  OR status IN ('pending', 'scheduled', 'cancelled')
  OR (tracking_code IS NOT NULL AND btrim(tracking_code) <> '')
);

ALTER TABLE matches DROP CONSTRAINT uq_matches_donation_necessity;
CREATE UNIQUE INDEX uq_matches_active_donation_necessity
  ON matches (donation_id, necessity_id)
  WHERE status NOT IN ('cancelled', 'rejected', 'completed');
