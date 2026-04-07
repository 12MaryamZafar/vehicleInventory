# Dealer & Vehicle Inventory Module

> A production-grade **multi-tenant inventory module** built inside a **Modular Monolith** using Java 21 and Spring Boot 3. Manages dealers and their vehicles with strict tenant isolation, role-based access control, dynamic filtering, and clean architecture boundaries.

![Java](https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange?style=flat-square&logo=mysql)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

---

## What Is This?

A **multi-tenant inventory system** where every dealer and vehicle belongs to a tenant (an isolated client/company). All data is scoped by `tenant_id`, enforced automatically via a `ThreadLocal` context populated from the `X-Tenant-Id` HTTP header on every request.

The module is structured as a **Modular Monolith** — a single deployable JAR with clearly separated internal packages that respect clean architecture dependency rules.

---

## Project Structure

```
src/main/java/com/inventory/
│
├── shared/
│   ├── tenant/                  # TenantContext (ThreadLocal), TenantFilter
│   └── security/                # Role constants, GLOBAL_ADMIN guard
│
└── inventory/
    ├── api/                     # Controllers — HTTP boundary only
    │   ├── DealerController.java
    │   └── VehicleController.java
    │
    ├── application/             # Services — use case orchestration
    │   ├── DealerService.java
    │   └── VehicleService.java
    │
    ├── domain/                  # Entities, enums, value objects
    │   ├── Dealer.java
    │   ├── Vehicle.java
    │   ├── SubscriptionType.java  # BASIC | PREMIUM
    │   └── VehicleStatus.java     # AVAILABLE | SOLD
    │
    └── infrastructure/          # JPA repositories, Specifications
        ├── DealerRepository.java
        ├── VehicleRepository.java
        └── VehicleSpecification.java
```

---

## Data Model

### Dealer
| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `tenant_id` | String | Tenant isolation key |
| `name` | String | Required |
| `email` | String | Required, unique per tenant |
| `subscriptionType` | Enum | `BASIC` or `PREMIUM` |

### Vehicle
| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `tenant_id` | String | Tenant isolation key |
| `dealerId` | UUID | FK → Dealer |
| `model` | String | Required |
| `price` | Decimal | Must be positive |
| `status` | Enum | `AVAILABLE` or `SOLD` |

---

## API Endpoints

### Dealers — `/dealers`

| Method | Path | Description |
|---|---|---|
| `POST` | `/dealers` | Create a dealer for the current tenant |
| `GET` | `/dealers/{id}` | Fetch single dealer — cross-tenant → 403 |
| `GET` | `/dealers?page=&size=&sort=` | Paginated + sorted list, tenant-scoped |
| `PATCH` | `/dealers/{id}` | Partial update of dealer fields |
| `DELETE` | `/dealers/{id}` | Delete dealer within current tenant |

### Vehicles — `/vehicles`

| Method | Path | Description |
|---|---|---|
| `POST` | `/vehicles` | Create vehicle linked to a tenant dealer |
| `GET` | `/vehicles/{id}` | Fetch single vehicle — tenant-scoped |
| `GET` | `/vehicles?model=&status=&priceMin=&priceMax=&subscription=&page=&sort=` | Dynamic filters + pagination |
| `PATCH` | `/vehicles/{id}` | Partial update of vehicle fields |
| `DELETE` | `/vehicles/{id}` | Remove vehicle within tenant |

### Admin — `/admin` (GLOBAL_ADMIN only)

| Method | Path | Description |
|---|---|---|
| `GET` | `/admin/dealers/countBySubscription` | Returns `{"BASIC": n, "PREMIUM": n}` across **all tenants globally** |

> **Note on Admin Count**: This endpoint is intentionally **global** (not per-tenant). A `GLOBAL_ADMIN` operates above tenant scope by design. Regular tenant users cannot access this endpoint — it is protected by `@PreAuthorize("hasRole('GLOBAL_ADMIN')")`.

---

## Tenant & Security Rules

| Rule | Behaviour |
|---|---|
| Missing `X-Tenant-Id` header | Rejected at filter level → **HTTP 400** before any business logic |
| Cross-tenant access attempt | Service checks resource's `tenant_id` vs context → **HTTP 403** |
| `subscription=PREMIUM` filter | JOIN on dealer — returns vehicles of PREMIUM dealers **within caller's tenant only** |
| Admin endpoint without `GLOBAL_ADMIN` | Spring Security blocks → **HTTP 403** |

---

## Quick Start

### Prerequisites
- Java 21+
- MySQL 8.0+
- Maven 3.9+

### 1. Clone
```bash
git clone https://github.com/your-org/dealer-vehicle-inventory.git
cd dealer-vehicle-inventory
```

### 2. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vehicleinventory-db
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Run
```bash
./mvnw spring-boot:run
```

### 4. Test with Tenant Header
```bash
# Create a dealer
curl -X POST http://localhost:8080/dealers \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-abc" \
  -d '{"name":"AutoMax","email":"info@automax.com","subscriptionType":"PREMIUM"}'

# List vehicles for PREMIUM dealers only
curl -H "X-Tenant-Id: tenant-abc" \
  "http://localhost:8080/vehicles?subscription=PREMIUM&page=0&size=10"

# Missing tenant header → 400
curl http://localhost:8080/dealers
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Dynamic Queries | JPA Specifications (Criteria API) |
| Security | Spring Security — role-based + tenant enforcement |
| Build Tool | Maven |
| Architecture | Modular Monolith — Clean Architecture |

---

## Architecture Decisions

**Why Modular Monolith?**
Single deployable JAR with microservice-like internal boundaries. Easier to start, simpler to operate, clear path to extract modules later if needed.

**Why ThreadLocal for tenant context?**
The `X-Tenant-Id` header needs to be available deep in the service and repository layers without polluting every method signature. A `TenantContext` backed by `ThreadLocal` is the standard pattern — set in a `OncePerRequestFilter`, cleared in `finally` to prevent thread-pool leakage.

**Why JPA Specifications for vehicle filters?**
The vehicle listing has 5+ optional filters plus pagination and sorting. Static JPQL can't handle this cleanly. The Criteria API via `Specification<Vehicle>` allows composable, type-safe dynamic queries with no if/else string building.

---

## Roadmap

- [ ] Project scaffold & module structure
- [ ] Tenant filter + TenantContext
- [ ] Domain entities (Dealer, Vehicle)
- [ ] Repositories with tenant-scoped queries
- [ ] Dealer service + controller
- [ ] Vehicle service + dynamic filters
- [ ] Admin endpoint + GLOBAL_ADMIN security
- [ ] Global exception handler
- [ ] Integration tests

---

## License

MIT License — see [LICENSE](LICENSE) for details.
