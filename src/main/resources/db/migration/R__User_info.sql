-- Drop table before reapplying sql scripts
DROP TABLE if exists user_details_table CASCADE;

-- Create a table that has all teh user information needed
CREATE TABLE user_details_table (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    created_date date NOT NULL,
    is_admin boolean default true,
    profile_picture bytea
);

-- Admin account in the database with "admin" as encrypted password
INSERT INTO user_details_table VALUES (gen_random_uuid(), 'admin', 'admin', 'admin@com', '$2a$05$qURyvfv6eJtoroIDL48ExuAeOGxN705UcTgrHYyrrDjBLm5UrBDgO', '2020-10-15', true, null);
