[⬅ Volver al README Principal](../../README.md)

---

## 5. TECNOLOGÍAS Y DEPENDENCIAS

La plataforma tecnológica de `ms-playlist` está definida en `pom.xml`, `application.yml` y `Dockerfile` sobre **Spring Boot 3.3.4** y **Java 21**, alineada con el ecosistema StyleRadar para garantizar aislamiento hexagonal, migraciones reproducibles, mensajería resiliente sobre TLS y validación automática de calidad en CI/CD.

| Capa / Componente | Tecnología | Versión | Justificación Técnica |
| :--- | :--- | :--- | :--- |
| **Lenguaje Core** | Java | `21` (LTS) | Versión de soporte extendido (*Long-Term Support*); habilita `records`, *pattern matching* y mejoras de rendimiento de la JVM. |
| **Framework Principal** | Spring Boot (`spring-boot-starter-parent`) | `3.3.4` | Provee inyección de dependencias, servidor embebido Tomcat (`context-path: /api/v1`) y starters oficiales (`web`, `validation`, `data-jpa`, `amqp`, `actuator`). |
| **Acceso a Datos / ORM** | Spring Data JPA / Hibernate | `Gestionada por Spring Boot 3.3.4` | Implementa el adaptador de persistencia (`PlaylistRepositoryPort`) sobre entidades JPA relacionales manteniendo el dominio desacoplado (`open-in-view: false`, `ddl-auto: validate`). |
| **Pool de Conexiones** | HikariCP | `Gestionada por Spring Boot 3.3.4` | Limitado a `maximum-pool-size: 3` y `minimum-idle: 1` (`idle-timeout: 60000`, `initialization-fail-timeout: 60000`) para proteger el PostgreSQL compartido en QA/PROD. |
| **Base de Datos Relacional** | PostgreSQL (`postgres:16-alpine` + driver `postgresql`) | `16` (driver gestionado por Boot `3.3.4`) | Motor relacional ACID idóneo para preservar la integridad entre `playlists`, `playlist_prendas`, `playlist_likes` y `playlist_guardados` bajo *Database-per-Service*. |
| **Migraciones de Esquema** | Flyway (`flyway-core`, `flyway-database-postgresql`) | `Gestionada por Spring Boot 3.3.4` | Versiona de forma determinística las tablas e índices de `styleradar_playlist` desde `classpath:db/migration`. |
| **Mensajería Asíncrona** | RabbitMQ (`rabbitmq:3.13.7-management-alpine` + Spring AMQP) | `3.13.7` | Publica eventos de dominio hacia `styleradar.exchange` y consume eventos de retiro de catálogo con soporte de *Dead Letter Exchange* (`styleradar.dlx`). |
| **Cliente HTTP Síncrono** | Spring Cloud OpenFeign (`spring-cloud-starter-openfeign`) | `2023.0.3` | Cliente HTTP declarativo con timeouts `5s`/`15s` para consultar prendas en `ms-item-catalogo` (`GET /api/v1/garments/{id}`). |
| **Mapeo de DTOs y Entidades** | MapStruct (`mapstruct` + `mapstruct-processor`) | `1.5.5.Final` | Genera mapeadores en tiempo de compilación sin reflexión entre modelos de dominio, DTOs REST y entidades JPA. |
| **Documentación OpenAPI** | `springdoc-openapi-starter-webmvc-ui` | `2.6.0` | Expone automáticamente el contrato OpenAPI 3 (`/api/v1/v3/api-docs`) y Swagger UI (`/api/v1/swagger-ui.html`). |
| **Pruebas Unitarias e Integración** | JUnit 5, Mockito (`spring-boot-starter-test`) y Testcontainers (`junit-jupiter`, `postgresql`, `rabbitmq`) | `Boot 3.3.4` / Testcontainers `1.21.4` | Valida reglas de dominio de forma aislada y prueba adaptadores reales de PostgreSQL y RabbitMQ en contenedores efímeros. |
| **Métricas de Cobertura** | JaCoCo (`jacoco-maven-plugin`) | `0.8.12` | Mide cobertura de líneas (`LINE`) sobre `BUNDLE` (excluyendo `PlaylistServiceApplication`, `config/**`, `dto/**` y `entity/**`) y exige mínimo **`85 %`** (`0.85`). |
| **Análisis Estático y Calidad** | SpotBugs (`spotbugs-maven-plugin`) y SonarCloud (`sonar-maven-plugin`) | `4.8.6.4` / `4.0.0.4121` | SpotBugs inspecciona el bytecode (`effort: Max`, `threshold: High`) y SonarCloud (`styleradar1` / `StyleRadar1_RST-Playlist-Service`) evalúa calidad y deuda técnica consumiendo `jacoco.xml`. |
| **Contenerización** | Docker (OCI Multi-stage) | Build: `maven:3.9.9-eclipse-temurin-21-alpine`<br>Runtime: `eclipse-temurin:21.0.5_11-jre-alpine` | Construye el JAR `ms-playlist-0.0.1-SNAPSHOT.jar` en etapa `build`, importa el certificado `rabbitmq-ca.pem` al `cacerts` de la JVM, ejecuta bajo usuario no-root `app` y expone el puerto `8260`. |

---

[⬅ Volver al README Principal](../../README.md)
