CREATE TABLE matches (
  donation_id integer NOT NULL,
  necessity_id integer NOT NULL,
  status record_status NOT NULL DEFAULT 'registered',
  created_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT pk_matches PRIMARY KEY (donation_id, necessity_id),
  CONSTRAINT fk_matches_donation FOREIGN KEY (donation_id) REFERENCES donations (id) ON DELETE CASCADE,
  CONSTRAINT fk_matches_necessity FOREIGN KEY (necessity_id) REFERENCES necessities (id) ON DELETE CASCADE
);

CREATE INDEX ON matches (necessity_id);
