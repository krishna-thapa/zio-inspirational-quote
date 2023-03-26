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

```
redis-cli -a redis123

// Get all the lists
KEYS * 

// Delete the list
DEL cache-quoteOfTheDay

// Get all the stored values inside the list
LRANGE cache-random-quote 0 -1
LRANGE cache-quoteOfTheDay 0 -1
```

## Use of Doobie for the JDBC
- [Doobie](https://tpolecat.github.io/doobie/)
- [Use of ZIO with Doobie](https://zio.dev/guides/interop/with-cats-effect/#using-zio-with-doobie)
- [Sample project](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/persistence/UserPersistenceService.scala)

## Links to resources
- [Javax email in ZIO](https://github.com/funcit/zio-email)
- [GitHub action with Scala native packager](https://stackoverflow.com/questions/64666502/sbt-native-packager-push-to-github-actions-repository)



## Run the project as Docker container
- To publish it locally: `sbt docker:publishLocal`
- To run the docker image: `docker run --network="host" <image id>`

## Further work stories
- [x] Create a user table for the Authorization service layer
- [x] Add the Author details to the postgres table
- [x] Add the favorite quote API endpoints for the users
- [x] Use of the fs2/ZIO Stream for reading the quotes from the postgres as stream
- [ ] Add the Swagger API management tool
- [x] Add the Postgres full text search and new API endpoint for it
- [ ] Add the Redis for the cache management and to handle the uniqueness on the random quotes
- [ ] Dockerized the whole app and deploy the image to the GitHub docker hub
- [ ] Start writing some test cases and add integration test using test containers
- [ ] See how the email notification can be achieved using publisher and subscriber pattern with scheduler as cron job
- [ ] Use of the opaque type of Scala 3
- [ ] Add email system for the registration confirmation and password recovery