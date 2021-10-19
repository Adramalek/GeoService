-- !Ups

CREATE TABLE IF NOT EXISTS geo_web (
    tile_x INT NOT NULL,
    tile_y INT NOT NULL,
    distance_error REAL NOT NULL,
    CONSTRAINT geo_web_pk PRIMARY KEY (tile_x, tile_y)
);

-- !Downs

DROP TABLE IF EXISTS geo_web;
