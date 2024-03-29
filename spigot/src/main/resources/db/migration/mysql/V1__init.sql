CREATE TABLE IF NOT EXISTS chatty_users (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS chatty_ignored_users (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    player_uuid VARCHAR(36) NOT NULL
        REFERENCES chatty_users (uuid),
    ignored_uuid VARCHAR(36) NOT NULL
        REFERENCES chatty_users (uuid),
    CONSTRAINT chatty_ignored_users_uq UNIQUE (player_uuid, ignored_uuid)
);

CREATE INDEX chatty_users_username_idx ON chatty_users (username);
CREATE INDEX chatty_ignored_users_player_uuid_idx ON chatty_ignored_users (player_uuid);
CREATE INDEX chatty_ignored_users_ignored_uuid_idx ON chatty_ignored_users (ignored_uuid);