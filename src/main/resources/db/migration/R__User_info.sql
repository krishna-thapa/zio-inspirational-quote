-- Drop tables before reapplying sql scripts
DROP TABLE if exists user_details_table CASCADE;
DROP TABLE if exists user_fav_quotes CASCADE;

-- Create a table that has all the user information needed
CREATE TABLE user_details_table
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      varchar(255) NOT NULL,
    last_name       varchar(255) NOT NULL,
    email           varchar(255) NOT NULL UNIQUE,
    password        varchar(255) NOT NULL,
    created_date    date         NOT NULL,
    is_admin        boolean          default true,
    is_notification boolean          default true,
    profile_picture bytea
);

-- Create a table that has references of favourite quotes for each of the user
CREATE TABLE user_fav_quotes
(
    id      serial PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_details_table ON DELETE CASCADE,
    csv_id  text NOT NULL,
    fav_tag boolean default true,
    FOREIGN KEY (user_id) REFERENCES user_details_table (id)
);

-- Admin account in the database with "admin" as encrypted password
INSERT INTO user_details_table
VALUES (gen_random_uuid(), 'admin', 'admin', 'admin@com',
        '$2a$05$qURyvfv6eJtoroIDL48ExuAeOGxN705UcTgrHYyrrDjBLm5UrBDgO', '2020-10-15', true, false, null);
