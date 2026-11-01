-- Keep the offered item independent from subsequent inventory edits.
-- Older proposals retain the best data available at migration time; past edits cannot be reconstructed.
ALTER TABLE matches
    ADD COLUMN offered_title varchar(120),
    ADD COLUMN offered_condition varchar(40),
    ADD COLUMN offered_description text,
    ADD COLUMN offered_category varchar(60),
    ADD COLUMN offered_total_quantity integer;

UPDATE matches m
SET offered_title = i.title,
    offered_condition = i.condition::text,
    offered_description = i.description,
    offered_category = t.category::text,
    offered_total_quantity = p.quantity
FROM donations d
JOIN packages p ON p.id = d.package_id
JOIN items i ON i.id = p.item_id
JOIN item_types t ON t.id = i.item_type_id
WHERE m.donation_id = d.id;

ALTER TABLE matches
    ALTER COLUMN offered_title SET NOT NULL,
    ALTER COLUMN offered_condition SET NOT NULL,
    ALTER COLUMN offered_category SET NOT NULL,
    ALTER COLUMN offered_total_quantity SET NOT NULL;
