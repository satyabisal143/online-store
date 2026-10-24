-- Runs once, the first time the postgres volume is created.
-- Each microservice owns its own database (no shared tables between services).
CREATE DATABASE productdb;
CREATE DATABASE orderdb;
