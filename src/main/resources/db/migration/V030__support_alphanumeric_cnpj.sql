ALTER TABLE institutions DROP CONSTRAINT ck_institutions_cnpj_format;
ALTER TABLE institutions ADD CONSTRAINT ck_institutions_cnpj_format
    CHECK (cnpj ~ '^[A-Z0-9]{12}[0-9]{2}$');
