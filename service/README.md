# service

Quarkus application module structured with the BCE (Boundary-Control-Entity) pattern. Business components organize code by domain responsibilities, with boundary layers for external interaction (JAX-RS resources), control for procedural logic, and entity for domain objects.

The base package is `airhacks.jdk`; its `package-info.java` is the system doc, each business component's `package-info.java` its spec.

## Prerequisites

Java 25, Maven, Docker for Quarkus Dev Services. Without Docker, a reachable PostgreSQL (see Test).

## Build and Run

```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

Development mode:

```bash
mvn quarkus:dev
```

PostgreSQL is the store. Development mode and tests start it through Quarkus Dev Services; production reads `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME` and `QUARKUS_DATASOURCE_PASSWORD`. Flyway applies the DDL under `src/main/resources/db/migration` at startup; one file per business component per change, versioned by timestamp.

## Persistence

Plain JDBC with Data-Oriented Programming. The records in `entity` are the row shapes and carry the invariants. The controls hold the SQL in text blocks, bind parameters with `?`, and map rows by column name through the canonical constructor. Resource classes carry the `@Boundary` stereotype from `airhacks.jdk`, which bundles `@ApplicationScoped` and `@Transactional`, so every boundary method runs in a container-managed transaction. Boundaries inject the controls and never see a `DataSource`; each control method takes its connection from the pool, which enlists it in the boundary's transaction. The DDL is the source of truth: a schema change starts in SQL and the record follows.

A `SQLException` is translated once, in the control, into the JAX-RS exception the runtime answers. The boundary catches nothing and the module defines no `ExceptionMapper`. Every error body is `{"error": "<message>"}`.

| SQLState | Cause | Response |
|---|---|---|
| `08xxx` | store unreachable | 503 |
| `23505` | unique violation | 409, `duplicate: <constraint>` |
| `23503` | foreign key violation | 409, `unknown reference: <constraint>` |
| any other, including `23502` and `23514` | statement failed, or record invariants and DDL disagree | 500 |

The constraint name comes from the driver message, so every constraint and index in the DDL is named.

## Test

```bash
mvn test
```

Control and boundary tests run as `@QuarkusTest` against PostgreSQL from Dev Services; the persistence mapping is covered by round trips, `save` followed by `find` yields an equal record. The SQLState translation and input parsing are plain JUnit tests.

To run without Docker, point the tests at an existing PostgreSQL. Dev Services then stays off, and the tests delete and reseed the `releases` tables of that database:

```bash
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/quarkus \
QUARKUS_DATASOURCE_USERNAME=quarkus \
QUARKUS_DATASOURCE_PASSWORD=quarkus \
mvn test
```
