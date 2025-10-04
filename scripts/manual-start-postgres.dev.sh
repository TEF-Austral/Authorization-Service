#!/usr/bin/env bash

docker run --name=authorization_db --rm -p 5437:5432 \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_USER=sa \
  -e POSTGRES_DB=authorization_db \
  postgres:16