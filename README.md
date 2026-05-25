# Advanced Warehouse Management System (WMS)

A state-of-the-art, enterprise-grade Multi-Tenant Warehouse Management System (WMS) designed to optimize warehouse storage, track real-time inventory movements, register dedicated tenants with isolated workspaces, manage users with granular roles, and provide full auditability via real-time transactional ledgers.

Developed in compliance with the **Infotact Java Internship Track Project 1 brief**, this system is engineered for maximum throughput, sub-200ms scan workflows, and strict data security.

---

## 🚀 Key Features

### 🏢 Multi-Tenant Data Isolation
* **Strict Context Boundary**: All core domain entities—including Orders, Products, Warehouses, Storage Bins, Users, and Audit Logs—are logically isolated based on the authenticated user's warehouse context.
* **Zero Leakage**: Cross-tenant queries are completely blocked. Operators and Admins can only view and manage resources explicitly belonging to their designated warehouse.

### 🔑 Unified Registration & Auto-Provisioning
* **Self-Registration Flow**: New administrative accounts can register directly through the redesigned login portal.
* **On-the-fly Warehouse Creation**: Upon sign-up, the system automatically provisions a brand new, dedicated warehouse tenant/workspace for the administrator.

### 👥 Granular User & Role Management
* **Role-Based Access Control (RBAC)**: Secure access limits features based on two roles:
  * `ADMIN`: Full control over the designated warehouse, including operator creation, inventory management, product catalogs, and order dispatching.
  * `OPERATOR`: Access to daily inventory operations, QR scanning, receiving, picking, packing, and shipping workflows.
* **Operator Management Dashboard**: Admins can create, activate, and manage operator accounts locked specifically to their warehouse scope.

### 📊 Real-Time Audit Logging & Ledger
* **Transactional Ledger**: Every inventory adjustment, putaway, or shipment records an immutable audit ledger entry.
* **Live System Audit Log**: Dedicated dashboard logs activity in real-time, filtered securely by warehouse, ensuring complete transparency and compliance.

### 📦 Core WMS Engine
* **Hierarchical Storage Model**: Organized as `Warehouse` ➡️ `Zone` ➡️ `Aisle` ➡️ `StorageBin` with detailed spatial coordinates.
* **Smart Receiving & Putaway**: Transactional backend service matches incoming SKUs to candidate bins based on capacity and compatibility, executing writes atomically.
* **Dynamic QR Code Engine**: Generates high-fidelity QR codes automatically using ZXing for every SKU upon product registration, enabling seamless scanning.
* **Robust Order Fulfillment**: Complete order lifecycle tracking: `PENDING` ➡️ `PICKING` ➡️ `PACKED` ➡️ `SHIPPED`.
* **Pessimistic Concurrency Controls**: Prevents double-allocation using database write locks (`PESSIMISTIC_WRITE`) and transaction rollbacks on `InsufficientStockException`.

### 🚚 Fleet Management & Route Optimization
* **Fleet & Driver Registry**: Registers vehicles with different payload capacities, fuel types (Diesel, Petrol, Electric, CNG), and tracks active drivers with shifts and vehicles.
* **Delivery Task Lifecycle**: Full tracking of outbound stops: `UNASSIGNED` ➡️ `DISPATCHED` ➡️ `IN_TRANSIT` ➡️ `DELIVERED` / `FAILED` with state machine validation.
* **Route Optimization Engine**: Integrates with Open Source Routing Machine (OSRM) free APIs for distance matrices, solving the Traveling Salesperson Problem (TSP) using Nearest Neighbor and 2-opt search heuristics.
* **Dynamic Fuel & Duration Estimations**: Auto-calculates trip distance, optimized waypoint order, driving times, and fuel consumption based on fuel type.
* **Consolidated Manifests**: Generates structured, print-ready delivery manifests containing sequential stop orders, customer details, and special instructions.

---

## 🛠️ Technology Stack

* **Backend Engine**: Java 17+, Spring Boot 3, Spring Web, Spring Security (JWT-based stateless auth), Spring Data JPA, PostgreSQL
* **Frontend Portal**: React 18, Vite, Vanilla CSS with custom glassmorphism design tokens, Lucide React Icons
* **Testing Suite**: JUnit 5, Mockito, Spring Boot Integration Tests with H2 in-memory DB
* **DevOps & Infrastructure**: Docker Compose, GitHub Actions CI/CD pipeline

---

## 🐳 Run With Docker

```bash
docker compose up --build
```

Then open:

- **Frontend Portal**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### Seed Accounts (For Instant Testing)

- **Admin**: `admin` / `admin123`
- **Operator**: `operator` / `operator123`

---

## 💻 Run Locally

### Backend Requirements (Java 17+ and Maven)

```bash
cd backend
mvn spring-boot:run
```

### Frontend Requirements (Node 22+)

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

---

## ⚙️ Environment Configuration

Copy `.env.example` to `.env` for Docker usage and replace the JWT secret before sharing or deploying.

Required production values:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `WMS_JWT_SECRET`
- `JWT_EXPIRES_MINUTES`

---

## 🔄 API Flow

1. **Login**: Login with `POST /api/auth/login` to obtain JWT.
2. **Register Admin**: `POST /api/auth/register` creates an administrator account and auto-provisions a new Warehouse.
3. **Manage Operators**: Admin creates operators with `POST /api/users` (scoped to warehouse).
4. **Create Products**: Create products with `POST /api/products` (QR code auto-generated).
5. **Receive Stock**: Receive inbound stock with `POST /api/inventory/receive` (atomically puts away to bins).
6. **Create Orders**: Create orders with `POST /api/orders`.
7. **Fulfill Orders**: Move orders through `/start-picking`, `/pack`, and `/ship` (updates ledger and decrements stock).
8. **View Audit Logs**: View secured logs with `GET /api/audit-logs`.

Example HTTP requests are documented in `docs/api.http`.

---

## 📋 PDF Requirement Alignment

* **Project Scope**: Project 1, Enterprise Warehouse Management System.
* **Warehouse Model**: `Warehouse -> Zone -> Aisle -> StorageBin`.
* **Core Entities**: `Product`, `Warehouse`, `StorageBin`, `InventoryItem`, orders, users, suppliers, product categories, and purchase orders.
* **Receiving and Putaway**: Transactional service assigns inbound stock to bins with available capacity.
* **Inventory Integrity**: Pessimistic write locks and `@Transactional` methods protect stock changes.
* **Barcode/QR Support**: ZXing generates QR images for product SKUs.
* **Fulfillment**: Orders move through `PENDING -> PICKING -> PACKED -> SHIPPED`; packing decrements stock and raises `InsufficientStockException` when stock is unavailable.
* **Security**: Spring Security with JWT and role-based `ADMIN` / `OPERATOR` access.
* **Frontend**: Redesigned glassmorphic React dashboard consumes the Spring Boot REST APIs.
* **Database**: PostgreSQL for local and Docker runtime; H2 is used only by automated tests.
* **CI/CD**: GitHub Actions runs backend tests and frontend production build.
* **Multi-Tenant Isolation**: Complete logical tenant isolation based on user's associated Warehouse context.
* **User Management**: Administrative capability to register and manage operators per warehouse.
* **Audit Logging**: Real-time, tenant-isolated transactional audit trail for absolute transparency.
* **Fleet Management**: Fully featured fleet vehicle registry, driver shift planning, and task delivery assignments.
* **Route Optimization**: Algorithmic route optimization engine integrating OSRM table/route APIs with TSP heuristic, reducing driving time and estimating fuel needs.

---

## ⚡ Response Time & Performance Check

The PDF target specifies that API response times should remain below 200 ms for scanning workflows. With the backend running, use this helper to take a quick local measurement:

```powershell
.\scripts\check-api-performance.ps1 -BaseUrl http://localhost:8081 -Iterations 10
```

The script logs in with the seeded admin account, calls common API endpoints, and reports average/max response time with a pass/fail status against the 200 ms target.

