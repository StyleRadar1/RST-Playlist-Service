[⬅ Volver al README Principal](../../README.md)

---

## 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `ms-playlist` (`RST-Playlist-Service`)
* **Paquete Arquitectónico:** `Paquete 2: Catálogo`
* **Puerto de Escucha Oficial:** `8260` (con `context-path: /api/v1`)
* **Base de Datos Asignada:** `PostgreSQL 16` (`styleradar_playlist`), aislada bajo el patrón *Database-per-Service*.
* **Caché:** No aplica (el servicio no utiliza caché distribuida en memoria como Redis; persiste y consulta directamente en `PostgreSQL 16`).
* **Bounded Context:** `Playlists de estilo`

### Responsabilidades del Dominio

Este microservicio encapsula exclusivamente la lógica de negocio de las playlists de estilo creadas por los usuarios compradores de StyleRadar, incluyendo su visibilidad, asociación de prendas con instantánea de catálogo (`PrendaSnapshot`), interacciones sociales (Me gusta y Guardados) y curaduría de playlists destacadas.

| Qué HACE este Microservicio (Alcance) | Qué NO HACE este Microservicio (Límites) |
| :--- | :--- |
| • Crea, edita y elimina playlists públicas o privadas asociadas a un usuario comprador. | • NO autentica usuarios ni emite tokens JWT (delegado a `ms-auth` / `ms-users`). |
| • Agrega y retira hasta un máximo de 15 prendas por playlist sin duplicados. | • NO publica, modera ni edita prendas (delegado a `ms-item-catalogo` en el puerto `:8220`). |
| • Consulta información de prendas en `ms-item-catalogo` vía Feign para construir `PrendaSnapshot` y desactiva prendas retiradas vía evento AMQP. | • NO gestiona inventario ni reservas por talla (delegado a `ms-inventario` en el puerto `:8200`). |
| • Registra los *Me gusta* y *Guardados* de los usuarios y calcula las playlists más populares. | • NO procesa pagos, notificaciones ni chats de segunda mano (delegado a `ms-pagos`, `ms-notifications` y `ms-chat`). |
| • Permite destacar hasta un máximo de 3 playlists para visibilidad en plataforma. | • NO accede directamente a bases de datos de otros microservicios. |

### Catálogo de Requerimientos Funcionales Oficiales

1. **`SR-RF-69` (`RES-17`)** · *Crear y configurar playlist*: creación, consulta pública/privada, edición y eliminación de playlists por su dueño (`POST /api/v1/playlists`, `GET /api/v1/playlists`, `GET /api/v1/playlists/{id}`, `GET /api/v1/playlists/users/{userId}`, `PUT /api/v1/playlists/{id}`, `DELETE /api/v1/playlists/{id}`).
2. **`SR-RF-70` (`RES-18`)** · *Agregar o retirar prendas de playlist*: asociación de hasta 15 prendas únicas validadas contra `ms-item-catalogo` y retiro de prendas (`POST /api/v1/playlists/{id}/garments`, `DELETE /api/v1/playlists/{id}/garments/{garmentId}`).
3. **`SR-RF-71` (`RES-19`)** · *Interactuar con playlist*: alternancia de *Me gusta* y *Guardado* por usuario, consulta de guardadas y ranking de popularidad (`POST /api/v1/playlists/{id}/like`, `POST /api/v1/playlists/{id}/save`, `GET /api/v1/playlists/users/{userId}/saved`, `GET /api/v1/playlists/[PENDIENTE: confirmar ruta exacta en inglés para populares]`).
4. **`SR-RF-72` (`RES-20`)** · *Destacar playlists populares*: marcación y desmarcación administrativa de playlists destacadas con límite máximo de 3 activas (`GET /api/v1/playlists/featured`, `PUT /api/v1/playlists/{id}/feature`, `DELETE /api/v1/playlists/{id}/feature`).

### Contexto de Marca / Arquetipo

<div align="center">
  <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/mateo.jpg" width="130" style="border-radius:10px;" alt="Mateo (Calle) - Arquetipo ms-playlist" />
  <p><b>Mateo (Calle)</b> · <i>Playlists &amp; Cultura Urbana</i></p>
</div>

Dentro del ecosistema **StyleRadar**, `ms-playlist` representa el arquetipo **Mateo (Calle)**, orientado al descubrimiento curado de outfits y combinaciones de estilo urbano, formal, casual o deportivo. Su rol conecta la exploración del catálogo (`ms-item-catalogo`) con la fidelización social de compradores (`BUYER`) y la curaduría editorial de administradores (`STYLERADAR_ADMIN`).

---

[⬅ Volver al README Principal](../../README.md)
