## 📌 README.md – Projeto de Mensageria com Kafka (Java)

### 🖥️ Disciplina: Software Concorrente e Distribuído

### 📚 Curso: Bacharelado em Engenharia de Software

---

## ✅ Resumo do Projeto:

Simulamos uma arquitetura de **microserviços distribuídos com mensageria**, usando **Apache Kafka como backbone**.

**Fluxo:**

1. O **Order-Service** publica pedidos no tópico Kafka `orders`.
2. O **Inventory-Service** consome esses pedidos, faz o processamento de estoque e publica um evento no tópico `inventory-events`.
3. O **Notification-Service** consome o `inventory-events` e simula o envio de uma notificação (log no console).

---

## ✅ Requisitos para rodar:

- **Java 17 ou superior**
- **Apache Maven**
- **Docker** + **Docker Compose**
- Ferramenta para testar APIs REST (Postman, Insomnia ou curl)

---

## ✅ Passo a passo de execução:

### Atalho:

Para simplificar o processo, utilize o shell script "setup_inicial.sh", responsável por iniciar o compose e já criar os tópicos no Kafka.

```bash
./setup_inicial.sh
```

Com isso, pode pular os passos 1 e 2.

### 🚩 1. Subir o Apache Kafka via Docker

No terminal, vá até a pasta `docker/` do projeto:

```bash
cd docker
docker-compose up -d
```

Isso vai levantar:

- Zookeeper
- Kafka Broker

Kafka estará disponível em: `localhost:9092`

---

### 🚩 2. Criar os tópicos Kafka necessários:

Abra o terminal dentro do container Kafka:

```bash
docker exec -it kafka bash
```

Crie os tópicos:

````bash
kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

---

### 🚩 3. Rodar os serviços Java (cada um em um terminal separado):

#### 📌 Order-Service (porta: 8080):

```bash
cd order-service
mvn clean
mvn spring-boot:run
````

---

#### 📌 Inventory-Service (porta: 8081):

```bash
cd inventory-service
mvn clean
mvn spring-boot:run
```

---

#### 📌 Notification-Service (porta: 8082):

```bash
cd notification-service
mvn clean
mvn spring-boot:run
```

---

### 🚩 4. Testar o sistema usando Postman ou curl:

**Endpoint:**

```
POST http://localhost:8080/orders
```

**Corpo da requisição (JSON):**

```json
{
    "items": [
        { "sku": "SAMS-001", "quantity": 1 },
        { "sku": "MOUS-003", "quantity": 2 }
    ]
}'
```

**Exemplo via Curl com mensagem válida:**

```sh
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "sku": "SAMS-001", "quantity": 1 },
      { "sku": "MOUS-003", "quantity": 2 }
    ]
  }'
```

**Exemplo via Curl com mensagem inválida:**

```sh
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "sku": "SAMS-001", "quantity": 999 },
      { "sku": "MOUS-003", "quantity": 999 }
    ]
  }'
```

---

### ✅ Esperado para a mensagem válida:

1. Order-Service vai responder:
   "Order enviado com sucesso! ID: xxx"

2. Inventory-Service vai logar no terminal:
   Mensagem recebida + status (success ou failed)

3. Notification-Service vai logar no terminal:
   Notificação do evento de inventário

4. No banco de dados, deve-se observar uma redução no "quantity" de cada item, na tabela items.
