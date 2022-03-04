## Prerequisites

1. JDK 11+
2. Maven
3. Docker
4. *JAVA_HOME* configured appropriately

## To run

1. Run docker with postgres container:
```
docker run -it --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:14.1
```

2. Run the app itself:
```
./mvnw compile quarkus:dev
```

3. (Optional) Run the client in browser:
```
localhost:8080/
```

## Database alternatives

Database : Extension name : Pool class name : Placeholders

1. IBM Db2 : quarkus-reactive-db2-client : io.vertx.mutiny.db2client.DB2Pool : ?
2. MariaDB/MySQL : quarkus-reactive-mysql-client : io.vertx.mutiny.mysqlclient.MySQLPool : ?
3. Microsoft SQL Server : quarkus-reactive-mssql-client : io.vertx.mutiny.mssqlclient.MSSQLPool : @p1, @p2, etc.
4. Oracle : quarkus-reactive-oracle-client : io.vertx.mutiny.oracleclient.OraclePool : ?
5. PostgreSQL : quarkus-reactive-pg-client : io.vertx.mutiny.pgclient.PgPool : $1, $2, etc.

## Complete guide reference

https://quarkus.io/guides/reactive-sql-clients