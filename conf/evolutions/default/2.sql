# --- !Ups

CREATE TABLE t_car_advert (
  car_advert_id UUID PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  fuel VARCHAR(30) NOT NULL,
  price INTEGER NOT NULL,
  new BOOLEAN NOT NULL,
  mileage INTEGER,
  first_registration DATE,

  CONSTRAINT fk_car_advert_fuel_type FOREIGN  KEY (fuel) REFERENCES t_fuel_type(name)
);

# --- !Downs

DROP TABLE t_car_advert;