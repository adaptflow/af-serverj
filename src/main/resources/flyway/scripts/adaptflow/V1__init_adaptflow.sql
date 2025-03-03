CREATE SCHEMA IF NOT EXISTS af_global;

CREATE TABLE IF NOT EXISTS af_global.providers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS af_global.services (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider_id UUID NOT NULL,
    FOREIGN KEY (provider_id) REFERENCES af_global.providers(id) ON DELETE CASCADE,
    UNIQUE (provider_id, name)
);

CREATE TABLE IF NOT EXISTS af_global.credentials (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider_id UUID NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    FOREIGN KEY (provider_id) REFERENCES af_global.providers(id) ON DELETE CASCADE,
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS af_global.credential_services (
    credential_id UUID NOT NULL,
    service_id UUID NOT NULL,
    PRIMARY KEY (credential_id, service_id),
    FOREIGN KEY (credential_id) REFERENCES af_global.credentials(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES af_global.services(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS af_global.user (
    id uuid PRIMARY KEY,
    createdat bigint NOT NULL,
    deletedat bigint,
    email character varying(255) NOT NULL UNIQUE,
    firstname character varying(255) NOT NULL,
    lastlogin bigint,
    lastname character varying(50),
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL UNIQUE
);
------------------------------------------------------------------------------------------------
-- Add Sample Data------------------------------------------------------------------------------
-- Clear existing data
DELETE FROM af_global.credential_services;
DELETE FROM af_global.credentials;
DELETE FROM af_global.services;
DELETE FROM af_global.providers;
DELETE FROM af_global.user;

-- Insert sample providers
INSERT INTO af_global.providers (id, name) VALUES
    ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'AWS'),
    ('1f5b5e60-df85-4f60-b1b4-1a3c77f1fbd3', 'Google Cloud'),
    ('8d3f9f9f-e7d9-46fd-a0b2-5f3e80e240a1', 'Azure');

-- Insert sample services
INSERT INTO af_global.services (id, name, provider_id) VALUES
    ('0f5d2a52-c3d0-4c4b-b3cc-d85e3345d8f2', 'EC2', '6ba7b810-9dad-11d1-80b4-00c04fd430c8'),
    ('d7a211df-7170-4f5d-b8b3-2d2d1ef9788d', 'S3', '6ba7b810-9dad-11d1-80b4-00c04fd430c8'),
    ('c245ebee-1535-4cf3-9618-bce98d4b6b7a', 'Compute Engine', '1f5b5e60-df85-4f60-b1b4-1a3c77f1fbd3'),
    ('9d5c6b89-1339-4d8e-b983-5b4e87d0361b', 'Cloud Storage', '1f5b5e60-df85-4f60-b1b4-1a3c77f1fbd3'),
    ('c7a03f55-2c73-4657-88a7-c62dffdd3654', 'Virtual Machines', '8d3f9f9f-e7d9-46fd-a0b2-5f3e80e240a1'),
    ('3f21c5b3-c603-47a9-8a19-8a8e717df6e3', 'Blob Storage', '8d3f9f9f-e7d9-46fd-a0b2-5f3e80e240a1');

-- Insert sample credentials
INSERT INTO af_global.credentials (id, name, provider_id, api_key) VALUES
    ('3d1c735b-c4d3-4cfb-91e2-7c5d8b3b9cc0', 'AWS Credential 1', '6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'aws_api_key_123'),
    ('b417bd61-74b7-4d7e-99a1-d3b2c53b1425', 'Google Cloud Credential 1', '1f5b5e60-df85-4f60-b1b4-1a3c77f1fbd3', 'gcp_api_key_456'),
    ('adddbab2-d23c-4f5f-8a52-d9fa91c22b2e', 'Azure Credential 1', '8d3f9f9f-e7d9-46fd-a0b2-5f3e80e240a1', 'azure_api_key_789');

-- Insert sample data into the credential_services table
INSERT INTO af_global.credential_services (credential_id, service_id) VALUES
    ('3d1c735b-c4d3-4cfb-91e2-7c5d8b3b9cc0', '0f5d2a52-c3d0-4c4b-b3cc-d85e3345d8f2'),  -- AWS Credential 1 -> EC2
    ('3d1c735b-c4d3-4cfb-91e2-7c5d8b3b9cc0', 'd7a211df-7170-4f5d-b8b3-2d2d1ef9788d'),  -- AWS Credential 1 -> S3
    ('b417bd61-74b7-4d7e-99a1-d3b2c53b1425', 'c245ebee-1535-4cf3-9618-bce98d4b6b7a'),  -- Google Cloud Credential 1 -> Compute Engine
    ('b417bd61-74b7-4d7e-99a1-d3b2c53b1425', '9d5c6b89-1339-4d8e-b983-5b4e87d0361b'),  -- Google Cloud Credential 1 -> Cloud Storage
    ('adddbab2-d23c-4f5f-8a52-d9fa91c22b2e', 'c7a03f55-2c73-4657-88a7-c62dffdd3654'),  -- Azure Credential 1 -> Virtual Machines
    ('adddbab2-d23c-4f5f-8a52-d9fa91c22b2e', '3f21c5b3-c603-47a9-8a19-8a8e717df6e3');  -- Azure Credential 1 -> Blob Storage

-- Insert default user into the user table
INSERT INTO af_global.user (id, createdat, email, firstname, password, username) VALUES
    ('5e2010a1-9c81-471a-88fb-aa416797124d', 1740416746909, 'admin@gmail.com', 'admin', '$2a$10$xgcnTqnJhAzOTOqLc7g.Puo7ma0KQLOfFUYcu8sjp6gORsojKTFq.', 'admin123');
    -- password for default user -> qwertyuiop