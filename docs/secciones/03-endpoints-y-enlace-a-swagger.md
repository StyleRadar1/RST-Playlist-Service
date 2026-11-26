[⬅ Volver al README Principal](../../README.md)

---

## 3. ENDPOINTS Y ENLACE A SWAGGER

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | Propósito | URL Base del Servicio | Swagger OpenAPI UI | Health Check |
| :--- | :---: | :--- | :--- | :--- | :--- |
| **Producción (PROD)** | Desplegado | Despliegue productivo de StyleRadar desde `master` | `https://rst-playlist-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1` | [Swagger UI PROD](https://rst-playlist-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/swagger-ui.html) | [`/api/v1/health`](https://rst-playlist-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/health) |
| **Calidad / Pruebas (QA)** | Desplegado | Pruebas integradas del Sprint desde `develop` | `https://rst-playlist-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1` | [Swagger UI QA](https://rst-playlist-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/swagger-ui.html) | [`/api/v1/health`](https://rst-playlist-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/health) |
| **Desarrollo Local (DEV)** | Local | Entorno de desarrollo local | `http://localhost:8260/api/v1` | [Swagger UI Local](http://localhost:8260/api/v1/swagger-ui.html) | [`/api/v1/health`](http://localhost:8260/api/v1/health) |

* **Especificación OpenAPI JSON (Local):** `http://localhost:8260/api/v1/v3/api-docs`

<div align="center">
  <p><em>[Captura de Swagger UI pendiente de adjuntar en docs/images/SwaggerEvidence.png]</em></p>
  <p><sub><b>Figura 3.1:</b> [PENDIENTE: capturar pantalla de Swagger UI de ms-playlist y guardarla en docs/images/SwaggerEvidence.png]</sub></p>
</div>

### Tabla de Endpoints Expuestos

Todos los endpoints siguen estrictamente las convenciones RESTful de la auditoría del ecosistema: rutas exclusivamente en inglés bajo `/api/v1`, sin `/me` y sin verbos en las URIs. La identidad del usuario se lee desde `X-User-Id` (propagado por el API Gateway), y la autorización de administrador en `/featured` se lee desde `X-User-Role` y `X-User-Roles` (`STYLERADAR_ADMIN` o `ADMIN_STYLERADAR`).

| Método | Endpoint | Descripción | Rol / Cabecera Requerida | Códigos HTTP |
| :---: | :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/playlists` | Crear playlist de moda (mínimo 1 prenda aprobada · `SR-RF-69`) | `X-User-Id` obligatorio | `201`, `400`, `401`, `404`, `422`, `503` |
| `GET` | `/api/v1/playlists` | Listar playlists públicas ordenadas por creación descendente | Público | `200` |
| `GET` | `/api/v1/playlists/{id}` | Consultar detalle de una playlist por UUID | Público (`X-User-Id` obligatorio si es `PRIVADA`) | `200`, `400`, `403`, `404` |
| `GET` | `/api/v1/playlists/featured` | Consultar hasta 3 playlists destacadas para Home (`SR-RF-72`) | Público | `200` |
| `GET` | `/api/v1/playlists/popular` | Consultar playlists públicas populares paginadas (`page`, `size`) | Público | `200` |
| `PUT` | `/api/v1/playlists/{id}` | Editar nombre, descripción, visibilidad o estilo de una playlist propia | `X-User-Id` (propietario) | `200`, `400`, `401`, `403`, `404` |
| `DELETE` | `/api/v1/playlists/{id}` | Eliminar una playlist propia | `X-User-Id` (propietario) | `204`, `401`, `403`, `404` |
| `PUT` | `/api/v1/playlists/{id}/featured` | Fijar playlist como destacada (máximo 3 · `SR-RF-72`) | `X-User-Id` + `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR` | `200`, `401`, `403`, `404`, `409` |
| `DELETE` | `/api/v1/playlists/{id}/featured` | Quitar playlist de destacadas | `X-User-Id` + `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR` | `200`, `401`, `403`, `404` |
| `POST` | `/api/v1/playlists/{id}/garments` | Asociar prenda aprobada a la playlist (máximo 15 · `RNF-12`) | `X-User-Id` (propietario) | `200`, `400`, `401`, `403`, `404`, `422`, `503` |
| `DELETE` | `/api/v1/playlists/{id}/garments/{garmentId}` | Retirar prenda de la playlist | `X-User-Id` (propietario) | `200`, `401`, `403`, `404`, `422` |
| `POST` | `/api/v1/playlists/{id}/likes` | Alternar Me Gusta sobre la playlist | `X-User-Id` obligatorio | `200`, `401`, `404` |
| `POST` | `/api/v1/playlists/{id}/saves` | Alternar guardado en perfil de usuario | `X-User-Id` obligatorio | `200`, `401`, `404` |
| `GET` | `/api/v1/playlists/users/{userId}` | Listar playlists creadas por el usuario autenticado | `X-User-Id` igual a `{userId}` | `200`, `401`, `403` |
| `GET` | `/api/v1/playlists/users/{userId}/saved` | Listar playlists guardadas por el usuario autenticado | `X-User-Id` igual a `{userId}` | `200`, `401`, `403` |
| `GET` | `/api/v1/health` | Estado de salud del servicio (`status`, `service`, `port`) | Público | `200`, `500` |

> [!NOTE]
> **Dependencia síncrona con `ms-item-catalogo`:** Las operaciones `POST /api/v1/playlists` y `POST /api/v1/playlists/{id}/garments` invocan por OpenFeign `GET {ITEM_CATALOGO_URL}/api/v1/garments/{id}` (timeouts de 5 s conexión / 15 s lectura). Si la prenda no existe en catálogo retorna `404 PRENDA_NO_ENCONTRADA`; si existe pero no está en estado `APROBADA` retorna `422 PRENDA_NO_APROBADA`; y si `ms-item-catalogo` no responde o está en arranque en frío por encima de 15 s, el servicio degrada de forma controlada devolviendo `503 CATALOGO_NO_DISPONIBLE`.

<details>
<summary><b>Click para ver Ejemplos Interactivos de Request y Response JSON (Payloads)</b></summary>

#### 1. Crear Playlist (`POST /api/v1/playlists`)

```bash
curl -X POST http://localhost:8260/api/v1/playlists \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d '{
    "nombre": "Streetwear Bogotá",
    "descripcion": "Básicos urbanos para clima frío",
    "visibilidad": "PUBLICA",
    "estilo": "URBANO",
    "prendasIds": [
      "83114431-f823-47a1-b2db-6a62f70a780c"
    ]
  }'
```

**Respuesta Exitosa (`201 Created`):**

```json
{
  "id": "b1b7e6d2-4d30-4f8f-9d9c-1f2e3a4b5c6d",
  "usuarioId": "15",
  "nombre": "Streetwear Bogotá",
  "descripcion": "Básicos urbanos para clima frío",
  "visibilidad": "PUBLICA",
  "estilo": "URBANO",
  "destacada": false,
  "fechaCreacion": "2026-11-24T16:15:00Z",
  "prendas": [
    {
      "prendaId": "83114431-f823-47a1-b2db-6a62f70a780c",
      "nombrePrenda": "Hoodie gris jaspeado",
      "fechaAsociacion": "2026-11-24T16:15:00Z",
      "disponible": true
    }
  ],
  "likes": 0,
  "guardados": 0,
  "totalPrendas": 1,
  "liked": false,
  "saved": false
}
```

Cuando la petición llega con `X-User-Id` (el API Gateway lo envía si hay token, también en las consultas públicas), `liked` y `saved` indican si ese usuario ya dio Me Gusta o guardó la playlist. Sin usuario ambos salen en `false`. Aplica a las consultas, a los likes y a los guardados.

#### 2. Respuesta de Error Estándar RFC 7807 (`400 Bad Request` · Playlist sin prendas)

```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "La playlist debe crearse con al menos una prenda (SR-RF-69)",
  "instance": "/api/v1/playlists",
  "code": "PLAYLIST_SIN_PRENDAS",
  "timestamp": "2026-11-24T16:16:10Z"
}
```

#### 3. Respuesta de Error de Autorización (`403 Forbidden` · Destacar sin rol administrador)

```json
{
  "type": "about:blank",
  "title": "Acceso denegado",
  "status": 403,
  "detail": "Solo un administrador de StyleRadar puede destacar o quitar destacado de una playlist",
  "instance": "/api/v1/playlists/b1b7e6d2-4d30-4f8f-9d9c-1f2e3a4b5c6d/featured",
  "code": "ACCESO_DENEGADO",
  "timestamp": "2026-11-24T16:17:00Z"
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
