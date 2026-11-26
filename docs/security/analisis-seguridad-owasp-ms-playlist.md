[⬅ Volver al README Principal](../../README.md)

---

## ANÁLISIS DE SEGURIDAD, HABEAS DATA Y OWASP TOP 10:2025 — `ms-playlist`

Este documento detalla los controles de seguridad implementados en **`ms-playlist`** (`RST-Playlist-Service`, puerto `8260`, prefijo `/api/v1`) conforme a la **Ley 1581 de 2012 (Habeas Data)** y el estándar **OWASP Top 10:2025**.

---

### 1. Cumplimiento de Ley 1581 de 2012 (Habeas Data)

| Principio | Implementación Real en `ms-playlist` | Evidencia en Código |
| :--- | :--- | :--- |
| **Minimización de datos** | No almacena nombres civiles, correos, documentos, teléfonos ni credenciales. Solo persiste `usuario_id` opaco propagado por el API Gateway y el snapshot básico de prenda (`prenda_id`, `nombre_prenda`). | `PlaylistEntity.java`, `PrendaEmbeddable.java` |
| **Privacidad en eventos** | Los eventos publicados en `styleradar.exchange` (`styleradar.playlist.creada`, `destacada.actualizada`, `prenda.asociada`) transportan únicamente UUIDs (`playlistId`, `prendaId`) y marca temporal. | `RabbitMQEventPublisher.java` |
| **Control de acceso a datos privados** | Las playlists `PRIVADA` solo pueden ser consultadas por su dueño. Las consultas `/users/{userId}` y `/users/{userId}/saved` validan coincidencia estricta entre `X-User-Id` y `{userId}`. | `Playlist.validarAccesoLectura`, `PlaylistController.validarMismoUsuario` |
| **Derecho de supresión** | El titular puede eliminar su playlist completa o retirar prendas asociadas en cualquier momento. | `DELETE /api/v1/playlists/{id}`, `DELETE /api/v1/playlists/{id}/garments/{garmentId}` |

---

### 2. Matriz OWASP Top 10:2025

| Categoría OWASP 2025 | Riesgo Mitigado en `ms-playlist` | Control Implementado | Cobertura en Pruebas |
| :--- | :--- | :--- | :--- |
| **A01:2025 · Pérdida de Control de Acceso (incl. SSRF)** | Edición/eliminación de playlists ajenas, acceso a playlists privadas, consulta de otro `userId`, destacado sin rol admin y SSRF hacia catálogo. | `Playlist.validarPropietario`, `Playlist.validarAccesoLectura`, `PlaylistController.validarMismoUsuario`, `PlaylistController.validarRolAdmin` (`STYLERADAR_ADMIN` / `ADMIN_STYLERADAR`) e `ItemCatalogoFeignClient` con URL fija (`ITEM_CATALOGO_URL`) y parámetro tipado `UUID` hacia `/api/v1/garments/{id}`. | `PlaylistTest`, `PlaylistControllerTest` |
| **A02:2025 · Configuración de Seguridad Incorrecta** | Exposición de endpoints internos, trazas de error o sesiones ORM abiertas en vistas. | `open-in-view: false`, `ddl-auto: validate`, Actuator limitado a `health, info`, respuesta genérica en `500 ERROR_INTERNO` y cero secretos quemados en `application.yml`. | `GlobalExceptionHandlerTest`, `application.yml` |
| **A03:2025 · Fallas en la Cadena de Suministro** | Dependencias vulnerables o builds no reproducibles. | Maven Wrapper (`./mvnw`), BOM de Spring Boot y análisis estático con SpotBugs en CI/CD. | Configuración Maven / CI |
| **A04:2025 · Fallas Criptográficas** | Tráfico sin cifrar hacia BD, broker o catálogo en QA/PROD. | `DB_SSL_MODE`, RabbitMQ AMQPS puerto `5671` con `RABBITMQ_SSL_ENABLED=true` y `RABBITMQ_SSL_VERIFY_HOSTNAME=true`, e integración HTTPS hacia catálogo. | `application.yml` |
| **A05:2025 · Inyección** | Inyección SQL o entradas malformadas en REST. | Spring Data JPA y JPQL parametrizado (`@Param("prendaId")`), Jakarta Validation (`@NotBlank`, `@NotNull`, `@Size`) y tipado `UUID` estricto. | `SpringDataPlaylistRepository`, `GlobalExceptionHandlerTest` |
| **A06:2025 · Diseño Inseguro** | Abuso de límites de negocio o saturación de conexiones en PostgreSQL compartido. | Invariantes de dominio (`MAX_PRENDAS = 15`, `MAX_DESTACADAS = 3`, mínimo 1 prenda aprobada, descripción ≤ 300) y pool HikariCP acotado (`maximum-pool-size: 3`, `minimum-idle: 1`). | `PlaylistTest`, `UseCasesTest`, `application.yml` |
| **A07:2025 · Fallas de Autenticación** | Peticiones anónimas sobre operaciones protegidas. | Validación obligatoria de `X-User-Id` (`401 USUARIO_NO_AUTENTICADO`), delegando validación de JWT al API Gateway. | `PlaylistControllerTest`, `GlobalExceptionHandlerTest` |
| **A08:2025 · Integridad de Software y Datos** | Deserialización insegura AMQP o desalineación de esquema SQL. | Remoción de cabecera `__TypeId__` en `RabbitMqConfig`, lectura segura en `CatalogoEventsConsumer` (`UUID.fromString`) y esquema gestionado con Flyway + `ddl-auto: validate`. | `CatalogoEventsConsumerTest`, `RabbitMqConfigTest`, `PlaylistServiceApplicationIntegrationTest` |
| **A09:2025 · Fallas de Registro y Monitoreo** | Falta de trazabilidad ante fallos sin fuga de secretos. | Logs estructurados en `GlobalExceptionHandler` (`log.error`), `ItemCatalogoClient` (`log.error`), `RabbitMQEventPublisher` (`log.warn`) y `CatalogoEventsConsumer` (`log.warn`). | `GlobalExceptionHandlerTest`, `CatalogoEventsConsumerTest` |
| **A10:2025 · Manejo Inadecuado de Condiciones Excepcionales** | Errores `500` cuando no hay RabbitMQ en PROD o fallos no normalizados. | Respuestas RFC 7807 en `GlobalExceptionHandler`, `503 CATALOGO_NO_DISPONIBLE` en `ItemCatalogoClient` y `try/catch (AmqpException)` en `RabbitMQEventPublisher.enviar(...)`. | `GlobalExceptionHandlerTest`, `ItemCatalogoClientTest`, `RabbitMQEventPublisherTest` |

---

[⬅ Volver al README Principal](../../README.md)
