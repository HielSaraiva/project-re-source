ALTER TABLE necessities ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE necessities ADD COLUMN deleted_at timestamptz;

-- Reconcile existing spelling variants before enforcing the identity used by the API.
-- All references are redirected, so no inventory, need or protocol is removed.
CREATE TEMP TABLE item_type_canonical ON COMMIT DROP AS
SELECT id, min(id) OVER (PARTITION BY category, lower(btrim(name))) AS canonical_id
FROM item_types;
UPDATE items i SET item_type_id = c.canonical_id
FROM item_type_canonical c WHERE i.item_type_id = c.id AND c.id <> c.canonical_id;
UPDATE necessities n SET item_type_id = c.canonical_id
FROM item_type_canonical c WHERE n.item_type_id = c.id AND c.id <> c.canonical_id;
DELETE FROM item_types t USING item_type_canonical c
WHERE t.id = c.id AND c.id <> c.canonical_id;
CREATE UNIQUE INDEX uq_item_types_category_normalized_name
ON item_types (category, lower(btrim(name)));
