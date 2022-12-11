## Overview

## Versions used
- Scala:
- sbt:
- JDK: 


- https://en.wikipedia.org/w/api.php?action=help&modules=query%2Ballimages
- https://en.wikipedia.org/w/api.php?action=help&modules=query

## Connect to Docker
### Connect to the postgres server running in docker container:
- First start the docker container with postgres image up and running `docker-compose up`
- Connect to the postgres through terminal `psql -h localhost -p 5432 -U admin postgres`
- Password for admin role is `admin`
- Connect to the right database `\c inspiration_quote_db`
- See all the tables `\dt`

**Create a user admin and make it as a super user**
```
CREATE USER admin WITH PASSWORD 'admin';
ALTER USER admin WITH SUPERUSER;
```

### Access to PgAdmin:
- URL: http://localhost:5050
- Username: `admin@admin.com`
- Password: `POSTGRES_PASSWORD`

**Add a new server in PgAdmin**
Host name/address `postgres`
Port `5432`
Username as POSTGRES_USER
Password as POSTGRES_PASSWORD