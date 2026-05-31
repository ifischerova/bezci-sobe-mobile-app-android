-- Users
CREATE TABLE users (
    id          UUID PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(50),
    last_name   VARCHAR(50),
    city        VARCHAR(50)
);

CREATE TABLE user_roles (
    user_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Reference tables
CREATE TABLE track_lengths (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE track_types (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE certifications (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE race_calendars (
    id        BIGSERIAL PRIMARY KEY,
    year      INTEGER   NOT NULL,
    is_active BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Races
CREATE TABLE races (
    id               BIGSERIAL    PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    place            VARCHAR(200) NOT NULL,
    date             DATE         NOT NULL,
    start_time       TIME         NOT NULL,
    web              VARCHAR(500),
    track_length_id  BIGINT       NOT NULL REFERENCES track_lengths(id),
    track_type_id    BIGINT       NOT NULL REFERENCES track_types(id),
    race_calendar_id BIGINT       REFERENCES race_calendars(id)
);

CREATE INDEX idx_races_date ON races(date);

CREATE TABLE race_certifications (
    race_id          BIGINT NOT NULL REFERENCES races(id) ON DELETE CASCADE,
    certification_id BIGINT NOT NULL REFERENCES certifications(id) ON DELETE CASCADE,
    PRIMARY KEY (race_id, certification_id)
);

-- Rides
CREATE TABLE rides (
    id              UUID         PRIMARY KEY,
    race_id         BIGINT       NOT NULL REFERENCES races(id),
    user_id         UUID         NOT NULL REFERENCES users(id),
    type            VARCHAR(10)  NOT NULL CHECK (type IN ('OFFER', 'REQUEST')),
    departure_from  VARCHAR(200) NOT NULL,
    destination_to  VARCHAR(200),
    car             VARCHAR(200),
    available_seats INTEGER      NOT NULL,
    occupied_seats  INTEGER      NOT NULL DEFAULT 0,
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_seats CHECK (occupied_seats <= available_seats)
);

CREATE INDEX idx_rides_race_id ON rides(race_id);
CREATE INDEX idx_rides_user_id ON rides(user_id);

CREATE TABLE ride_passengers (
    ride_id UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (ride_id, user_id)
);
