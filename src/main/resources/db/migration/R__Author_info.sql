-- Drop tables before reapplying sql scripts
DROP TABLE if exists author_details CASCADE;

-- Create a table that has all the author information needed
CREATE TABLE author_details
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       varchar(255) NOT NULL,
    alias       text[],
    description text[],
    imageUrl    varchar(255)
);