[⬅ Volver al README Principal](../../README.md)

---

## 6. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **JDK:** Java `21` (LTS).
* **Apache Maven:** `3.9+` (o wrapper incluido `./mvnw` / `mvnw.cmd`).
* **Docker Engine y Docker Compose** activos en el equipo.
* **PostgreSQL 16** y **RabbitMQ 3.13.7** disponibles si se ejecuta directamente con Maven en el host, además del archivo `.env` configurado en la raíz del repositorio y el certificado `rabbitmq-ca.pem` presente al construir la imagen Docker.

### Ejecución Local con Maven (Perfil Dev)

```bash
# 1. Clonar el repositorio oficial
git clone https://github.com/StyleRadar1/RST-Playlist-Service.git
cd RST-Playlist-Service

# 2. Crear el archivo .env (asegura DB_HOST=localhost, DB_NAME=styleradar_playlist, DB_USER=postgres y RABBITMQ_HOST=localhost)
cp .env.example .env

# 3. Verificar que PostgreSQL (puerto 5432) y RabbitMQ (puerto 5672) estén activos en localhost
# 4. Ejecutar el microservicio con el perfil dev en el puerto 8260 (context-path /api/v1)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> [!NOTE]
> **Parámetros de conexión en host local y comandos equivalentes en Windows PowerShell:**
> * En ejecución directa en host (`application.yml`), la aplicación escucha en **`localhost:8260`** bajo el prefijo **`/api/v1`**, y se conecta por defecto a PostgreSQL en `localhost:5432/styleradar_playlist` (usuario `postgres`), RabbitMQ en `localhost:5672` (usuario `guest`, vhost `/`) y `ms-item-catalogo` en `http://localhost:8220`.
> * **Importante sobre `docker-compose.yml`:** Los servicios `postgres` (`ms-playlist-db`) y `rabbitmq` (`ms-playlist-rabbitmq`) no publican los puertos `5432` ni `5672` hacia el host (solo `8260` en `ms-playlist` y `15672` en `rabbitmq`). Si corres la aplicación con Maven en el host en lugar de Docker Compose, asegúrate de tener PostgreSQL y RabbitMQ escuchando en `localhost:5432` y `localhost:5672`.
> * En **Windows PowerShell**:
> ```powershell
> Copy-Item .env.example .env
> .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
> Invoke-RestMethod -Uri http://localhost:8260/api/v1/health
> ```

### Ejecución Contenerizada Completa con Docker Compose

El archivo `docker-compose.yml` orquesta la pila completa (`ms-playlist`, `ms-playlist-db` y `ms-playlist-rabbitmq`) esperando los *healthchecks* de PostgreSQL y RabbitMQ antes de arrancar la aplicación:

<details>
<summary><b>Click para ver Comandos de Docker Compose y Tabla de Puertos/Redes</b></summary>

```bash
# Construir la imagen (multi-stage con rabbitmq-ca.pem) y levantar los 3 contenedores
docker compose up -d --build

# Verificar que los contenedores estén saludables (healthy / Up)
docker compose ps

# Consultar logs en tiempo real de ms-playlist
docker compose logs -f ms-playlist

# Detener y remover los contenedores (conservando el volumen db-data)
docker compose down
```

| Componente | Conexión en Host Local (Maven) | Conexión dentro de Docker Compose |
| :--- | :--- | :--- |
| **API `ms-playlist`** | `http://localhost:8260/api/v1` | `http://ms-playlist:8260/api/v1` *(publicado al host `${SERVER_PORT:-8260}:8260`)* |
| **PostgreSQL 16 (`ms-playlist-db`)** | `jdbc:postgresql://localhost:5432/styleradar_playlist` | `jdbc:postgresql://postgres:5432/${DB_NAME}` *(red interna Compose; `DB_HOST=postgres`)* |
| **RabbitMQ 3.13.7 (`ms-playlist-rabbitmq`)** | `localhost:5672` / UI `http://localhost:15672` | `rabbitmq:5672` *(red interna Compose; `RABBITMQ_HOST=rabbitmq`; UI `15672:15672`)* |
| **Cliente `ms-item-catalogo`** | `http://localhost:8220` | `http://host.docker.internal:8220` *(o URL del servicio en nube)* |

</details>

### Comprobación de Salud (Health Check)

```bash
curl -i http://localhost:8260/api/v1/health
```

**Respuesta esperada (`200 OK`):**
```json
{
  "status": "UP",
  "service": "ms-playlist",
  "port": 8260
}
```
---

[⬅ Volver al README Principal](../../README.md)
