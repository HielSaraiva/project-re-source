CREATE TYPE condition_status AS ENUM (
  'good',
  'bad',
  'normal',
  'other'
);

CREATE TYPE record_status AS ENUM (
  'registered',
  'pending_approval',
  'completed',
  'awaiting_shipment',
  'other'
);

CREATE TYPE necessity_level AS ENUM (
  'high',
  'medium',
  'low'
);

CREATE TYPE item_category AS ENUM (
  'food',
  'clothing',
  'household',
  'toy',
  'other'
);
