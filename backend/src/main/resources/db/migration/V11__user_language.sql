-- V11__user_language.sql
ALTER TABLE users
    ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'cs';

ALTER TABLE users
    ADD CONSTRAINT users_language_check
    CHECK (language IN ('cs', 'en'));
