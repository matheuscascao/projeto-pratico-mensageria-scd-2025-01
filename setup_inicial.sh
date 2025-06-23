#!/bin/bash

set -e

echo "Iniciando os containers..."
docker-compose -f docker/docker-compose.yml up -d

echo "Aguardando o kafka..."
while ! docker exec kafka nc -z localhost 9092; do
  sleep 1
done

echo "Criando tópicos..."
docker exec kafka kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists
docker exec kafka kafka-topics --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists

echo "Tópicos criados com sucesso!"