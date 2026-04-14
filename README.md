# Expense Tracker - Backend

API REST construida con Spring Boot 4 + PostgreSQL.

## Stack
- Java 21
- Spring Boot 4
- Spring Security 7 + JWT
- PostgreSQL 16
- Docker

## Endpoints
| Método | Ruta | Auth |
|--------|------|------|
| POST | /api/auth/register | ❌ |
| POST | /api/auth/login | ❌ |
| GET | /api/categories | ✅ |
| GET | /api/expenses | ✅ |
| POST | /api/expenses | ✅ |
| PUT | /api/expenses/{id} | ✅ |
| DELETE | /api/expenses/{id} | ✅ |
| GET | /api/expenses/summary | ✅ |

## Cómo correr el proyecto

### 1. Levantar PostgreSQL
```bash
docker-compose -f compose.yaml up -d
```

### 2. Correr el proyecto
```bash
./mvnw spring-boot:run
```
