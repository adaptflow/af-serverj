CREATE SCHEMA IF NOT EXISTS af_global;

CREATE TABLE IF NOT EXISTS af_global.model (
    id UUID NOT NULL,
    js_model_json varchar,
    PRIMARY KEY (id)
);