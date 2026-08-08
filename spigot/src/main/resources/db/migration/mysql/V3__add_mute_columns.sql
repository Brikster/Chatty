ALTER TABLE chatty_users
    ADD COLUMN muted_until BIGINT;

ALTER TABLE chatty_users
    ADD COLUMN mute_reason VARCHAR(255);
