-- CSV Id starts from 101
CREATE SEQUENCE IF NOT EXISTS serial_csv_id START 101;

-- Create a table for quotes
CREATE TABLE IF NOT EXISTS quotes (
  id serial NOT NULL,
  csv_id text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csv_id'::regclass)::text,
  serial_id UUID NOT NULL UNIQUE,
  quote varchar (1000) NOT NULL,
  author json,
  genre text [],
  stored_date DATE NOT NULL
);