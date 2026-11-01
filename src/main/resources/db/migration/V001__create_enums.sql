CREATE TYPE account_role AS ENUM (
  'donor',
  'administrator'
);

CREATE TYPE account_status AS ENUM (
  'active',
  'pending',
  'blocked'
);

CREATE TYPE institution_status AS ENUM (
  'pending_approval',
  'approved',
  'rejected',
  'blocked'
);

CREATE TYPE institution_sector AS ENUM (
  'education',
  'health',
  'social_assistance',
  'environment',
  'other'
);

CREATE TYPE document_type AS ENUM (
  'identity_document',
  'organization_document'
);

CREATE TYPE document_status AS ENUM (
  'pending',
  'approved',
  'rejected'
);

CREATE TYPE item_condition AS ENUM (
  'new',
  'good',
  'used',
  'other'
);

CREATE TYPE item_category AS ENUM (
  'food',
  'clothing',
  'household',
  'toy',
  'education',
  'hygiene',
  'other'
);

CREATE TYPE necessity_status AS ENUM (
  'active',
  'fulfilled',
  'cancelled'
);

CREATE TYPE necessity_priority AS ENUM (
  'high',
  'medium',
  'low'
);

CREATE TYPE donation_status AS ENUM (
  'registered',
  'awaiting_acceptance',
  'accepted',
  'rejected',
  'awaiting_shipment',
  'in_transit',
  'completed',
  'cancelled'
);

CREATE TYPE match_status AS ENUM (
  'proposed',
  'awaiting_acceptance',
  'accepted',
  'rejected',
  'awaiting_shipment',
  'in_transit',
  'completed',
  'cancelled'
);

CREATE TYPE delivery_method AS ENUM (
  'in_person',
  'resource_logistics',
  'carrier'
);

CREATE TYPE delivery_status AS ENUM (
  'pending',
  'scheduled',
  'in_transit',
  'delivered',
  'cancelled'
);
