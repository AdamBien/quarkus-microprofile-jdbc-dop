#!/bin/zsh
docker rm -f jdbc-dop-postgres
docker run --rm --name jdbc-dop-postgres -e POSTGRES_USER=jdbc-dop -e POSTGRES_PASSWORD=jdbc-dop-secret -e POSTGRES_DB=jdbc-dop-db -p 5432:5432 -d postgres