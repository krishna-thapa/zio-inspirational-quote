CREATE TABLE quotes (
  serialId UUID NOT NULL PRIMARY KEY,
  quote VARCHAR(1000) NOT NULL,
  author json,
  genre TEXT []
);