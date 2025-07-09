-- init/01-schema.sql

CREATE TABLE property_sales (
    sale_id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    property_id           INTEGER,
    download_date         DATE,
    council_name          TEXT,
    purchase_price        INTEGER,
    address               TEXT,
    post_code             INTEGER,
    property_type         TEXT,
    strata_lot_number     INTEGER,
    property_name         TEXT,
    area                  NUMERIC(10, 2),
    area_type             TEXT,
    contract_date         DATE,
    settlement_date       DATE,
    zoning                TEXT,
    nature_of_property    TEXT,
    primary_purpose       TEXT,
    legal_description     TEXT
);

CREATE TABLE metrics (
    metric_id     TEXT NOT NULL,
    metric_name   TEXT NOT NULL,
    num_accessed  INTEGER DEFAULT 0,
    UNIQUE (metric_name, metric_id)
);