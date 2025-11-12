# Users & Posts — API (Laravel + Spring Boot) + Front (React)

Proyecto de prueba técnica: dos APIs gemelas (Laravel y Spring Boot) y un frontend en React (MUI). JWT, roles (`user`/`admin`), CRUD de usuarios y posts con imagen.

---

## Stack
- **Backend**: Spring Boot 3.3.x + Java 17/21 + MySQL + JWT (jjwt) + Docker Compose.
- **Frontend**: React + Vite + MUI + Axios.

## Estructura sugerida
```
/api-laravel
/api-spring
/frontend
/docs/postman (opcional)
```

---

## Requisitos
- Node 18+ (Vite)
- Java 17/21, Maven (si corrés Spring sin Docker)
- Docker + Docker Compose (recomendado para el backend Spring)
- MySQL 8.x (si corrés Spring sin Docker)

---

# 1) Backend Spring Boot (con Docker)

> Recomendado correrlo con Docker Compose. Si lo corrés sin Docker, ajustá `application.yml` para tu MySQL local y exportá `APP_JWT_SECRET` en tu entorno.

### 1.1. `docker-compose.yml` (ejemplo)
```yaml
services:
  api:
    build: .
    ports: ["8081:8081"]
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - APP_JWT_SECRET=supersecreto-superlargo-min32chars-para-hmac
    volumes:
      - ./uploads:/app/uploads
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=api_spring
      - MYSQL_ROOT_PASSWORD=root
    ports: ["3306:3306"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 10
```

### 2.2. `src/main/resources/application-docker.yml` (ejemplo)
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://db:3306/api_spring?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

app:
  jwt:
    secret: ${APP_JWT_SECRET:change-me}
    expirationSec: 3600
  upload-dir: /app/uploads
```

### 2.3. Levantar
```bash
cd api-spring
docker compose up -d --build
docker compose logs -f api
```
Esperá `Started ApiSpringApplication`.

### 2.4. Verificar secret
```bash
docker compose exec api printenv APP_JWT_SECRET
```

### 2.5. Smoke test (curl)
```bash
BASE=http://127.0.0.1:8081/api

# registrar
curl -X POST $BASE/auth/register -H "Content-Type: application/json"   -d '{"name":"Davi","email":"davi@test.com","password":"secret123"}'

# login
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json"   -d '{"email":"davi@test.com","password":"secret123"}' | jq -r .access_token)

# me
curl -H "Authorization: Bearer $TOKEN" $BASE/auth/me

# crear post (multipart)
curl -X POST "$BASE/posts" -H "Authorization: Bearer $TOKEN"   -F "title=Hola" -F "description=Mundo" -F "image=@/ruta/a/imagen.jpg"
```

---

# 3) Frontend (React + Vite + MUI)

### 3.1. .env
Elegí la API (Spring):
```
# para Spring
# VITE_API_BASE_URL=http://127.0.0.1:8081/api
```

### 3.2. Instalar y correr
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

---

# 4) Colección Postman/Insomnia
- Importá el archivo JSON de colección (si está en `/docs/postman/api-users-posts.postman_collection.json`).
- Variables:
  - `{{baseUrl}}`: `http://127.0.0.1:8000/api` (Laravel) o `http://127.0.0.1:8081/api` (Spring)
  - `{{token}}`: se setea solo al hacer **Login** (si usás Postman con el test script).

---

# 5) Endpoints (comunes a ambas APIs)
```
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me

GET    /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
DELETE /api/users/{id}

GET    /api/posts
GET    /api/posts/{id}
POST   /api/posts          (multipart: title, description, image)
PUT    /api/posts/{id}     (multipart opcional)
DELETE /api/posts/{id}
```

- Regla de permisos: **cualquiera autenticado** lista/ve. **Editar/Borrar**: dueño o **admin**.

---

# 6) Notas de seguridad
- **JWT secret**: 32+ caracteres. Cambiarla invalida tokens existentes.

---

# 7) Troubleshooting
- **401**: token inválido/expirado → relogin. Ver `APP_JWT_SECRET` cargada en Spring.
- **403**: intentás editar/borrar sin permisos. Checks (Spring).
- **CORS**: agrega `http://localhost:5173` y `http://127.0.0.1:5173`.
- **Imágenes no visibles**:
  - Spring: asegurar `app.upload-dir` y `ResourceHandler /files/**`.
- **MySQL collation**: si tu servidor no soporta `utf8mb4_0900_ai_ci`, usa `utf8mb4_unicode_ci`.
- **Puertos ocupados**: cambiá mapeos `8000/8081/3306` o liberá procesos.

---

## Licencia
Libre uso para evaluación.
