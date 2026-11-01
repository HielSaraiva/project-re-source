ALTER TABLE institutions ADD COLUMN phone varchar(30);

ALTER TABLE states
  ADD COLUMN abbreviation varchar(2),
  ADD CONSTRAINT uq_states_abbreviation UNIQUE (abbreviation),
  ADD CONSTRAINT ck_states_abbreviation CHECK (abbreviation ~ '^[A-Z]{2}$');

UPDATE states AS state
SET abbreviation = brazilian_state.abbreviation
FROM (VALUES
  ('Acre', 'AC'), ('Alagoas', 'AL'), ('Amapá', 'AP'), ('Amazonas', 'AM'),
  ('Bahia', 'BA'), ('Ceará', 'CE'), ('Distrito Federal', 'DF'),
  ('Espírito Santo', 'ES'), ('Goiás', 'GO'), ('Maranhão', 'MA'),
  ('Mato Grosso', 'MT'), ('Mato Grosso do Sul', 'MS'), ('Minas Gerais', 'MG'),
  ('Pará', 'PA'), ('Paraíba', 'PB'), ('Paraná', 'PR'), ('Pernambuco', 'PE'),
  ('Piauí', 'PI'), ('Rio de Janeiro', 'RJ'), ('Rio Grande do Norte', 'RN'),
  ('Rio Grande do Sul', 'RS'), ('Rondônia', 'RO'), ('Roraima', 'RR'),
  ('Santa Catarina', 'SC'), ('São Paulo', 'SP'), ('Sergipe', 'SE'),
  ('Tocantins', 'TO')
) AS brazilian_state(name, abbreviation)
WHERE lower(state.name) = lower(brazilian_state.name);

ALTER TYPE item_category ADD VALUE 'furniture';
ALTER TYPE item_category ADD VALUE 'bedding';
