## Overview

## Versions used
- Scala: 3.2.2
- sbt: 1.8.1
- JDK: JDK 11

## References
- https://en.wikipedia.org/w/api.php?action=help&modules=query%2Ballimages
- https://en.wikipedia.org/w/api.php?action=help&modules=query
- https://forestry.io/blog/full-text-searching-with-postgres/#indexing

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

**To kill the existing Postgres Container in Ubuntu**
```
sudo lsof -i :5432
sudo kill <pid>
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

## Use of Doobie for the JDBC
- [Doobie](https://tpolecat.github.io/doobie/)
- [Use of ZIO with Doobie](https://zio.dev/guides/interop/with-cats-effect/#using-zio-with-doobie)
- [Sample project](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/persistence/UserPersistenceService.scala)

## Further work stories
- [x] Create a user table for the Authorization service layer
- [ ] Add the Author details to the postgres table
- [x] Add the favorite quote API endpoints for the users
- [ ] Use of the fs2/ZIO Stream for reading the quotes from the postgres as stream
- [ ] Add the Swagger API management tool
- [ ] Add the Redis for the cache management and to handle the uniqueness on the random quotes
- [ ] Dockerized the whole app and deploy the image to the GitHub docker hub
- [ ] Start writing some test cases
- [ ] See how the email notification can be achieved using publisher and subscriber pattern