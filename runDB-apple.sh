#!/bin/zsh
# PostgreSQL via Apple's container CLI (macOS 26, Apple silicon): https://github.com/apple/container
container system start
container rm -f jdbc-dop-postgres
container run --rm --name jdbc-dop-postgres -e POSTGRES_USER=jdbc-dop -e POSTGRES_PASSWORD=jdbc-dop-secret -e POSTGRES_DB=jdbc-dop-db -p 5432:5432 -d postgres
