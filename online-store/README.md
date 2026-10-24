# Online Store - Microservices Learning Project

Spring Boot 3 (Java 21) microservices + React (Vite) frontend.

```
React (5173) --> API Gateway (8080) --+--> product-service (8081, H2)
                                      +--> order-service   (8082, H2) --calls--> product-service
        all services register with: discovery-service / Eureka (8761)
```

| Module | What it teaches |
|---|---|
| `discovery-service` | Service discovery (Eureka) - services find each other by name |
| `api-gateway` | Single entry point, routing (`lb://service-name`), CORS |
| `product-service` | REST CRUD, Spring Data JPA, validation, exception handling, `@WebMvcTest` |
| `order-service` | Service-to-service calls (`RestClient` + `@LoadBalanced`), records/DTOs, unit tests with Mockito |
| `frontend` | React hooks, `fetch`, talking to the gateway |

## Run the backend

From this folder (no Maven install needed - the wrapper downloads it):

```powershell
.\mvnw.cmd clean verify          # build + run tests
```

Start each in its own terminal, **in this order**:

```powershell
.\mvnw.cmd -pl discovery-service spring-boot:run
.\mvnw.cmd -pl product-service spring-boot:run
.\mvnw.cmd -pl order-service spring-boot:run
.\mvnw.cmd -pl api-gateway spring-boot:run
```
(Or open the project in IntelliJ and run each `*Application` class.)

Check: http://localhost:8761 (Eureka dashboard should list 3 services), http://localhost:8080/api/products

> If Maven fails with `PKIX path building failed` (corporate proxy/antivirus HTTPS inspection), run
> `$env:JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"` first.

## Run the frontend

Install Node.js LTS from https://nodejs.org, then:

```powershell
cd frontend
npm install
npm run dev
```
Open http://localhost:5173.

## API quick reference (via gateway, port 8080)

- `GET /api/products`, `GET /api/products/{id}`, `POST/PUT/DELETE /api/products`
- `GET /api/orders`, `POST /api/orders` with `{"items":[{"productId":1,"quantity":2}]}`

## Where to go next

1. Decrease product stock when an order is placed (and think about what happens if one call fails - sagas).
2. Replace H2 with PostgreSQL (Docker Compose).
3. Add Spring Security + JWT at the gateway, and a `user-service`.
4. Resilience4j circuit breaker/retry around `ProductClient`.
5. Config server, Docker images per service, then Kubernetes.
6. Messaging with Kafka/RabbitMQ (e.g. `OrderPlaced` event).
7. Observability: Actuator + Micrometer + Zipkin tracing.
