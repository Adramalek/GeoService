-- !Ups

CREATE TABLE IF NOT EXISTS user_marks (
    user_id BIGSERIAL,
    lon REAL NOT NULL,
    lat REAL NOT NULL,
    CONSTRAINT user_marks_pk PRIMARY KEY (user_id)
);

CREATE INDEX IF NOT EXISTS user_marks__coordinates_nui ON user_marks (lon, lat);

-- !Downs

DROP TABLE IF EXISTS user_marks;
DROP INDEX IF EXISTS user_marks__coordinates_nui
