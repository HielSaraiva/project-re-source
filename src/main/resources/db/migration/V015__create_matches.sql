CREATE TABLE matches (
  donation_id integer NOT NULL REFERENCES donations (id) ON DELETE CASCADE,
  necessity_id integer NOT NULL REFERENCES necessities (id) ON DELETE CASCADE,
  status record_status NOT NULL DEFAULT 'registered',
  created_at timestamptz NOT NULL DEFAULT now(),

  PRIMARY KEY (donation_id, necessity_id)
);

CREATE INDEX ON matches (necessity_id);
