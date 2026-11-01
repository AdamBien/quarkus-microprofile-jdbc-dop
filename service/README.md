# service

Quarkus application module structured with the BCE (Boundary-Control-Entity) pattern. Business components organize code by domain responsibilities, with boundary layers for external interaction (JAX-RS resources), control for procedural logic, and entity for domain objects.

The base package is `airhacks.jdk`; its `package-info.java` is the system doc, each business component's `package-info.java` its spec.

## Build

```bash
mvn clean package
```

## Run

PostgreSQL is the store. Development mode and tests start it through Quarkus Dev Services (Docker required); production reads `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME` and `QUARKUS_DATASOURCE_PASSWORD`. Flyway applies the DDL under `src/main/resources/db/migration` at startup.

Development mode:

```bash
mvn quarkus:dev
```

Production build:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```
