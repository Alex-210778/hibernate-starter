SET SEARCH_PATH = hibernate_example;

DROP TABLE users;

DROP TABLE profile;

CREATE TABLE company
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(128),
    lastname VARCHAR(128),
    birth_date DATE,
    username VARCHAR(128) UNIQUE,
    role VARCHAR(32),
    info JSONB,
    company_id INTEGER REFERENCES company(id)
);

CREATE TABLE profile
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    street VARCHAR(128),
    language CHAR(2)
);

DELETE FROM users
WHERE username LIKE 'sveta%';