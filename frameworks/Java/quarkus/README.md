# Quarkus Benchmarking Test

This is the Quarkus portion of a [benchmarking test suite](../) comparing a variety of web development platforms.

There is currently one repository implementation.
* [WorldRepository](src/main/java/io/quarkus/benchmark/repository/WorldRepository.java) is using Hibernate ORM,
the reference JPA implementation for Quarkus.

### Plaintext Test

* [Plaintext test source](src/main/java/io/quarkus/benchmark/resource/PlainTextResource.java)

### JSON Serialization Test

* [JSON test source](src/main/java/io/quarkus/benchmark/resource/JsonResource.java)

### Database Query Test

* [Database Query test source](src/main/java/io/quarkus/benchmark/resource/DbResource.java)

### Database Queries Test

* [Database Queries test source](src/main/java/io/quarkus/benchmark/resource/DbResource.java)

### Database Update Test

* [Database Update test source](src/main/java/io/quarkus/benchmark/resource/DbResource.java)

### Template rendering Test

* [Template rendering test source](src/main/java/io/quarkus/benchmark/resource/FortuneResource.java)

## Versions

* [Java OpenJDK 8](http://openjdk.java.net/) - JDK 8 compatible, but the docker image is using JDK11.
* [Quarkus TBD](https://quarkus.io) - currently using a snapshot build

## Test URLs

### Plaintext Test

    http://localhost:8080/plaintext

### JSON Encoding Test

    http://localhost:8080/json

### Database Query Test

    http://localhost:8080/db

### Database Queries Test

    http://localhost:8080/queries?queries=5

### Database Update Test

    http://localhost:8080/updates?queries=5

### Template rendering Test

    http://localhost:8080/fortunes

# External testing

During development it might be easier to start a PostgreSQL instance directly:

    sudo podman run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name HibernateTestingPGSQL -e POSTGRES_USER=benchmarkdbuser -e POSTGRES_PASSWORD=benchmarkdbpass -e POSTGRES_DB=hello_world -p 5432:5432 postgres:11

Then edit the `application.properties` resource, so to point to the database on localhost.

On first run make sure you set Hibernate to create the schema:

    quarkus.hibernate-orm.database.generation=drop-and-create

Build the application

    mvn clean package

Run the application

    ./start-app.sh

If the DB is empty, generate the testing database by hitting this endpoint once:

    http://127.0.0.1:8080/createdata

Generate load on the application:

     ~/sources/wrk2/wrk -c 100 -d 60 -R 400 http://localhost:8080/db

(or any of the other URLs meaningful for the benchmark)

