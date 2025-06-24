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

```bash
kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

---

### 🚩 3. Rodar os serviços Java (cada um em um terminal separado):

#### 📌 Order-Service (porta: 8080):

```bash
cd order-service
mvn clean
mvn spring-boot:run
```

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

### 🚩 4. Testar o sistema:

**Itens disponíveis:**
| Nome do item | SKU | Quantidade inicial |
|-----------|----------|----------|
| Smartphone Samsung | SAMS-001 | 50 |
| Notebook Dell | DELL-002 | 25 |
| Mouse Wireless | MOUS-003 | 100 |
| Teclado Mecânico | TECL-004 | 75 |
| Monitor 24" | MONI-005 | 30 |
| Headset Gamer | HEAD-006 | 60 |
| Webcam HD | WEBC-007 | 40 |
| Carregador USB-C | CARR-008 | 120 |

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
}
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

## Diagramas

### Diagrama de classes

```mermaid
classDiagram
    direction LR

    class OrderServiceApplication {
        +main(String[] args)
    }

    class OrderController {
        +createOrder(OrderRequest orderRequest): ResponseEntity<String>
        -generateOrderId(String timestamp, String message): String
    }

    class Order {
        -String orderId
        -String timestamp
        -String message
        +Order()
        +Order(String orderId, String timestamp, String message)
        +getOrderId(): String
        +setOrderId(String orderId): void
        +getTimestamp(): String
        +setTimestamp(String timestamp): void
        +getMessage(): String
        +setMessage(String message): void
    }

    class OrderRepository {
        <<interface>>
        +JpaRepository<Order, String>
    }

    class OrderRequest {
        -List~ItemRequest~ items
        +getItems(): List~ItemRequest~
        +setItems(List~ItemRequest~ items): void
        +toString(): String
    }

    class ItemRequest {
        -String sku
        -Integer quantity
        +getSku(): String
        +setSku(String sku): void
        +getQuantity(): Integer
        +setQuantity(Integer quantity): void
        +toString(): String
    }

    class InventoryServiceApplication {
        +main(String[] args)
    }

    class InventoryConsumer {
        +consume(String message): void
        -extractOrderId(String message): String
    }

    class Item {
        -Long id
        -String name
        -String sku
        -Integer quantity
        +Item()
        +Item(String name, String sku, Integer quantity)
        +getId(): Long
        +setId(Long id): void
        +getName(): String
        +setName(String name): void
        +getSku(): String
        +setSku(String sku): void
        +getQuantity(): Integer
        +setQuantity(Integer quantity): void
    }

    class ItemRepository {
        <<interface>>
        +JpaRepository<Item, Long>
        +findBySku(String sku): Optional~Item~
        +reduceQuantityAtomically(String sku, Integer amount): int
        +hasEnoughStock(String sku, Integer amount): Boolean
        +getCurrentQuantity(String sku): Optional~Integer~
    }

    class DataInitializer {
        +run(String... args): void
        -initializeItems(): void
    }

    class ProcessedMessage {
        -String orderId
        -String status
        -Instant processedAt
        -String details
        +ProcessedMessage()
        +ProcessedMessage(String orderId, String status, String details)
        +getOrderId(): String
        +setOrderId(String orderId): void
        +getStatus(): String
        +setStatus(String status): void
        +getProcessedAt(): Instant
        +setProcessedAt(Instant processedAt): void
        +getDetails(): String
        +setDetails(String details): void
    }

    class ProcessedMessageRepository {
        <<interface>>
        +JpaRepository<ProcessedMessage, String>
        +existsById(String orderId): boolean
    }

    class NotificationServiceApplication {
        +main(String[] args)
    }

    class NotificationConsumer {
        +consume(String message): void
    }

    OrderServiceApplication ..> OrderController
    OrderController ..> OrderRequest
    OrderController ..> OrderRepository
    OrderController ..> Order
    OrderRequest --o ItemRequest

    InventoryServiceApplication ..> DataInitializer
    InventoryServiceApplication ..> InventoryConsumer

    InventoryConsumer ..> ItemRepository
    InventoryConsumer ..> ProcessedMessageRepository
    InventoryConsumer ..> Item

    DataInitializer ..> ItemRepository
    DataInitializer ..> Item

    NotificationServiceApplication ..> NotificationConsumer

    OrderRepository --|> JpaRepository
    ItemRepository --|> JpaRepository
    ProcessedMessageRepository --|> JpaRepository

    OrderController "1" -- "1" KafkaTemplate
    InventoryConsumer "1" -- "1" KafkaTemplate
```

### Diagrama de sequência

```mermaid
sequenceDiagram
    participant Cliente
    participant OrderService
    participant Kafka
    participant InventoryService
    participant NotificationService

    Cliente->>OrderService: POST /orders
    OrderService->>Kafka: Publica evento "order.created"
    Kafka->>InventoryService: Consome evento "order.created"
    InventoryService->>InventoryService: Verifica estoque

    alt Estoque suficiente
        InventoryService->>Kafka: Publica evento "inventory.reserved"
    else Estoque insuficiente
        InventoryService->>Kafka: Publica evento "inventory.failed"
    end

    alt Order já processado
        OS-->>Cliente: "Order já processado!"
    else Novo Pedido
        OS->>ODB: Salva novo Order
        activate ODB
        ODB-->>OS: Confirmação
        deactivate ODB
        OS->>Kafka: Envia mensagem "orders" (orderId, items)
        activate Kafka
        OS-->>Cliente: "Order enviado com sucesso!"
    end

    Kafka->>NotificationService: Consome evento de inventário
    NotificationService->>NotificationService: Envia notificação ao cliente

```

### Diagrama de Caso de Uso

```mermaid
C4Context
    title Diagrama de Caso de Uso do Sistema de Mensageria

    System(inventory_service, "Inventory Service", "Gerencia o estoque de produtos.")
    SystemDb(order_db, "Banco de Dados de Pedidos", "Armazena informações sobre os pedidos.")
    System(order_service, "Order Service", "Gerencia a criação e envio de pedidos.")
    Person(customer, "Cliente")
    SystemDb(inventory_db, "Banco de Dados de Inventário", "Armazena informações sobre o estoque.")
    System(notification_service, "Notification Service", "Envia notificações sobre o processamento de pedidos.")
    SystemQueue(kafka, "Apache Kafka", "Plataforma de streaming de eventos para comunicação assíncrona.")

    Rel(customer, order_service, "Faz Pedido")
    Rel(order_service, order_db, "Salva Pedido")
    Rel(order_service, kafka, "Publica 'orders' (Pedido Criado)")
    Rel(inventory_service, kafka, "Consome 'orders'")
    Rel(inventory_service, inventory_db, "Verifica e Atualiza Estoque")
    Rel(inventory_service, inventory_db, "Salva ProcessedMessage para Idempotência")
    Rel(inventory_service, kafka, "Publica 'inventory-events' (Estoque Atualizado/Falha)")
    Rel(notification_service, kafka, "Consome 'inventory-events'")
    Rel(notification_service, customer, "Notifica Cliente (simulado)")
```

## Resposta dos RNFs

## 1. Escalabilidade

## **Estratégias de Escalabilidade com Kafka**

### **1. Escalabilidade Horizontal**

- **Vários Brokers**: Adicionar mais brokers ao cluster Kafka (por exemplo: kafka1, kafka2, kafka3).
- **Partições**: Criar tópicos com múltiplas partições (ex: 6 partições).
- **Replicação**: Definir um fator de replicação adequado para garantir tolerância a falhas.

### **2. Escalabilidade dos Consumidores**

- **Múltiplas Instâncias**: Executar várias instâncias do serviço, como `inventory-service`.
- **Grupos de Consumidores**: Consumidores com o mesmo `group-id` compartilham a carga automaticamente.
- **Processamento Paralelo**: Cada instância consome partições diferentes do tópico.

## **Resultados:**

- **Escalabilidade linear**: Dobrar o número de brokers tende a dobrar a vazão.
- **Tolerância a falhas**: O sistema continua operando mesmo com falhas em brokers ou consumidores.
- **Distribuição de carga**: As mensagens são distribuídas automaticamente entre as partições.

O Kafka permite escalar horizontalmente de forma eficiente. Basta adicionar mais brokers/consumidores para aumentar o throughput do sistema.

# 2. Tolerância à falha

É a capacidade do sistema **continuar funcionando** mesmo quando componentes falham (brokers, consumidores, rede, etc).

## **Tolerância à Falha no Kafka - Cenário de Falha Comum**

### **Situação**: Broker Principal Falha

```
Cluster inicial:
- kafka1 (líder da partição orders-0) FALHA
- kafka2 (réplica da partição orders-0) OK
- kafka3 (réplica da partição orders-0) OK
``

## **Como o broker (Kafka) trata a falha**

### **1. Eleição de Novo Líder**
```

Automático:

```
- kafka1 com falha (ex-líder)
- kafka2 OK (NOVO LÍDER)
- kafka3 OK (réplica)
```

### **2. Sem Perda de Dados**

- **Replicação**: Mensagens já enviadas estão seguras em kafka2 e kafka3
- **Consumidores**: Inventory Service continua processando normalmente

## **Na prática:**

**Durante falha do kafka1:**

- **Orders continuam sendo aceitos** (redirecionados para kafka2)
- **Inventory Service continua processando**
- **Nenhum pedido é perdido**
- **Usuário nem percebe a falha**

**Quando kafka1 volta:**

- **Sincroniza dados perdidos**
- **Volta a participar do cluster**
- **Load balancing automático**

## 3. Idempotência

Idempotência é a característica de que, quando uma operação ocorre repetidas vezes, com as mesmas entradas, produz o mesmo resultado de uma execução única.

- No cenário atual, poderia ser implementada em diversas camadas. Na camada de orders, o client poderia enviar algum id único para que a API não reprocessasse o mesmo pedido mais de uma vez do mesmo client.
- Na camada de orders e inventory, o id da mensagem é utilizado para garantir essa característica.
- Como a chave é feita com base na mensagem e no timestamp, caso a mesma mensagem seja enviada no mesmo momento do tempo, é barrada.
