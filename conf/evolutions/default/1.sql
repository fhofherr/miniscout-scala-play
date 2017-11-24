# --- !Ups

CREATE TABLE t_fuel_type (
  name VARCHAR (30) PRIMARY KEY
);

INSERT INTO t_fuel_type(name) VALUES ('Gasoline');
INSERT INTO t_fuel_type(name) VALUES ('Diesel');

# --- !Downs

DROP TABLE t_fuel_type;