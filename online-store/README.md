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

## Run everything locally with PostgreSQL

1. Install **Docker Desktop** (https://www.docker.com/products/docker-desktop), start it, then in this folder:
   ```powershell
   docker compose up -d        # PostgreSQL on :5432 (databases productdb + orderdb), pgAdmin on :5050
   .\run-local.ps1             # opens one window per service + the React dev server
   ```
   No Docker yet? `.\run-local.ps1 -H2` runs the same stack on in-memory H2 (data resets on restart).
2. Open http://localhost:5173, place an order, then look at the data:
   - pgAdmin: http://localhost:5050 -> Add Server -> host `host.docker.internal`, port `5432`, user/password `store`/`store`
   - or in a terminal: `docker exec -it onlinestore-postgres psql -U store -d productdb -c "select * from product;"`
3. The `postgres` Spring profile (`application-postgres.yml`) turns on `show-sql`, so every SQL statement is printed in the service window.
   Data now survives restarts; wipe it with `docker compose down -v`.

## Debugging step by step

**Backend (IntelliJ):** open `ProductServiceApplication` / `OrderServiceApplication` / `ApiGatewayApplication` / `DiscoveryServiceApplication`,
click the green arrow -> **Debug**. For PostgreSQL, edit the run configuration -> *Active profiles* = `postgres`.
Good breakpoints to learn the flow of one order:
1. `OrderController.create` - the JSON arrives here
2. `OrderService.create` - the loop that reserves stock
3. `ProductClient.reserve` - the HTTP call to product-service
4. `ProductService.reserve` (in the product-service debug session) - stock is reduced
5. `OrderService.releaseAll` - hit it by ordering more than is in stock for the 2nd item

**Frontend (browser):** F12 -> **Network** tab -> click a request to `localhost:8080/api/...` and read *Payload* and *Response*.
Put `debugger;` in `frontend/src/api.js` (inside `request`) to pause there with DevTools open.

**Without the UI:** `Invoke-RestMethod http://localhost:8080/api/products` and
`Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders -ContentType application/json -Body '{"items":[{"productId":1,"quantity":2}]}'`

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
