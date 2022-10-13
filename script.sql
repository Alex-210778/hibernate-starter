SET SEARCH_PATH = hibernate_example;

DROP TABLE users;

CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(128) UNIQUE,
    firstname VARCHAR(128),
    lastname VARCHAR(128),
    birth_date DATE,
    role VARCHAR(32),
    info JSONB
);

DELETE FROM users
WHERE username LIKE 'sveta%';