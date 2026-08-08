ALTER TABLE users
    ADD COLUMN muted_until BIGINT;

ALTER TABLE users
    ADD COLUMN mute_reason VARCHAR(255);
