-- Use of Repeatable Migrations from Flyway Database migration
-- TODO: Remove the Repeatable migration and use the Version migration

-- Drop table before reapplying sql scripts
drop table IF EXISTS inspirational_quotes CASCADE;
drop sequence if EXISTS serial_csv_id CASCADE;
drop trigger IF EXISTS tsvectorupdate on inspirational_quotes CASCADE;
drop index IF EXISTS quotes_search_idx;

-- CSV Id starts from 101
create sequence IF NOT EXISTS serial_csv_id START 101;

-- Create a table for quotes
create TABLE IF NOT EXISTS inspirational_quotes
(
    id           serial NOT NULL,
    csv_id       text   NOT NULL UNIQUE DEFAULT 'CSV' || nextval('serial_csv_id'::regclass)::text,
    serial_id    UUID   NOT NULL PRIMARY KEY,
    quote        text   NOT NULL,
    quote_tsv    tsvector,
    author       varchar(100),
    related_info varchar(500),
    genre        text[],
    stored_date  DATE   NOT NULL
);

-- Create a trigger that will update the TS vector column when adding any quote
create or replace trigger tsvectorupdate
    before insert or update
    on inspirational_quotes
    for each row
EXECUTE procedure
    tsvector_update_trigger(quote_tsv, 'pg_catalog.english', quote);

-- Creating the proper index (a GIN index for text search), it can improve the search speed
CREATE INDEX quotes_search_idx ON inspirational_quotes USING GIN (quote_tsv);
