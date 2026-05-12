# Advanced Warehouse Management System

Enterprise WMS project built from the PDF brief for Infotact's Java internship track. It implements the Project 1 scope: hierarchical warehouse storage, real-time inventory transactions, receiving and putaway, QR code generation, order fulfillment states, JWT security, tests, Docker, and CI.

## Stack

- Backend: Java 17+, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT, PostgreSQL
- Frontend: React, Vite, Fetch API, Lucide icons
- Testing: JUnit 5, Spring Boot Test, H2
- DevOps: Docker Compose, GitHub Actions

## Features

- Warehouse hierarchy: `Warehouse -> Zone -> Aisle -> StorageBin`
- Product catalog with generated QR code images for every SKU
- Transactional receiving service that locks candidate bins and updates stock atomically
- Inventory ledger rows by product, bin, and warehouse
- Fulfillment workflow: `PENDING -> PICKING -> PACKED -> SHIPPED`
- Stock decremented when an order is packed, with `InsufficientStockException` rollback behavior
- Role-based access for `ADMIN` and `OPERATOR`
- Seed users and sample warehouse data for fast testing

## Run With Docker

```bash
docker compose up --build
```

Then open:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Seed accounts:

- Admin: `admin` / `admin123`
- Operator: `operator` / `operator123`

## Run Locally

Backend requires Java 17+ and Maven.

```bash
cd backend
mvn spring-boot:run
```

Frontend requires Node 22+.

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to `http://localhost:8080`.

For this Windows workspace, portable Java/Maven can also be placed under `.tools` and started with the helper scripts:

```bash
scripts\run-backend-local.cmd
scripts\run-frontend-local.cmd
```

The helper backend script uses the `local` profile, PostgreSQL database `wms`, and backend port `8081`. The frontend helper points Vite to `http://localhost:8081/api`.

## Environment

Copy `.env.example` to `.env` for Docker usage and replace the JWT secret before sharing or deploying.

Required production values:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `WMS_JWT_SECRET`
- `JWT_EXPIRES_MINUTES`

## API Flow

1. Login with `POST /api/auth/login`.
2. Create or use seeded warehouse storage.
3. Create products with `POST /api/products`.
4. Receive inbound stock with `POST /api/inventory/receive`.
5. Create orders with `POST /api/orders`.
6. Move orders through `/start-picking`, `/pack`, and `/ship`.

Example requests are in `docs/api.http`.

## Evaluation Notes

The PDF requires continuous GitHub activity across four weeks. This repository is ready for that workflow, but the commit history itself should be built honestly over time using feature branches and pull requests. The included CI runs `mvn -B clean test` for the backend and a production build for the frontend.

## PDF Requirement Alignment

- Project scope: Project 1, Enterprise Warehouse Management System.
- Warehouse model: `Warehouse -> Zone -> Aisle -> StorageBin`.
- Core entities: `Product`, `Warehouse`, `StorageBin`, `InventoryItem`, orders, users, suppliers, product categories, and purchase orders.
- Receiving and putaway: transactional service assigns inbound stock to bins with available capacity.
- Inventory integrity: pessimistic write locks and `@Transactional` methods protect stock changes.
- Barcode/QR support: ZXing generates QR images for product SKUs.
- Fulfillment: orders move through `PENDING -> PICKING -> PACKED -> SHIPPED`; packing decrements stock and raises `InsufficientStockException` when stock is unavailable.
- Security: Spring Security with JWT and role-based `ADMIN` / `OPERATOR` access.
- Frontend: React dashboard consumes the Spring Boot REST APIs.
- Database: PostgreSQL for local and Docker runtime; H2 is used only by automated tests.
- CI/CD: GitHub Actions runs backend tests and frontend production build.

## Response Time Check

The PDF target says API response times should remain below 200 ms for scanning workflows. With the backend running, use this helper to take a quick local measurement:

```powershell
.\scripts\check-api-performance.ps1 -BaseUrl http://localhost:8081 -Iterations 10
```

The script logs in with the seeded admin account, calls common API endpoints, and reports average/max response time with a pass/fail status against the 200 ms target.
