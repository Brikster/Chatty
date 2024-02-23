CREATE TABLE IF NOT EXISTS users (
    uuid UUID PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    spy BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS ignored_users (
    player_uuid UUID NOT NULL
        REFERENCES users (uuid),
    ignored_uuid UUID NOT NULL
        REFERENCES users (uuid),
    CONSTRAINT ignored_users_uq UNIQUE (player_uuid, ignored_uuid)
);