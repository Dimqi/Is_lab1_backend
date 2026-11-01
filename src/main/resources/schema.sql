CREATE SEQUENCE IF NOT EXISTS coordinates_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS flat_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS house_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS coordinates (
    id BIGINT PRIMARY KEY DEFAULT nextval('coordinates_seq'),
    x DOUBLE PRECISION NOT NULL,
    y BIGINT NOT NULL
    );

CREATE TABLE IF NOT EXISTS house (
    id BIGINT PRIMARY KEY DEFAULT nextval('house_seq'),
    name VARCHAR(255) NOT NULL,
    year BIGINT CHECK (year > 0 AND year <= 292),
    number_of_flats_on_floor INT CHECK (number_of_flats_on_floor > 0),
    coordinates_id BIGINT REFERENCES coordinates(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS flats (
    id BIGINT PRIMARY KEY DEFAULT nextval('flat_seq'),
    name VARCHAR(255) NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    area INT NOT NULL CHECK (area > 0 AND area <= 763),
    price INT NOT NULL CHECK (price > 0),
    balcony BOOLEAN,
    time_to_metro_on_foot REAL CHECK (time_to_metro_on_foot > 0),
    number_of_rooms INT CHECK (number_of_rooms > 0),
    furnish VARCHAR(50) CHECK (furnish IN ('FINE', 'BAD', 'LITTLE')),
    view VARCHAR(50) CHECK (view IN ('YARD', 'PARK', 'BAD', 'NORMAL')),
    transport VARCHAR(50) CHECK (transport IN ('LITTLE', 'NORMAL', 'ENOUGH')),
    house_id BIGINT REFERENCES house(id) ON DELETE NO ACTION
    );