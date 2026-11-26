[⬅ Volver al README Principal](../../README.md)

---

## 4. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona de `ms-playlist` con el resto del ecosistema StyleRadar se realiza mediante **RabbitMQ 3.13.7** sobre el exchange oficial **`styleradar.exchange`** en formato JSON (`styleradar.rabbitmq` en `application.yml`). Los mensajes entrantes que fallan durante su procesamiento o deserialización se derivan por el exchange de mensajes muertos **`styleradar.dlx`** hacia la cola **`styleradar.playlist.dlq`**.

### Matriz de Eventos AMQP

| Tipo | Routing Key | Cola (Queue) | Microservicio Emisor / Receptor | Cuándo se Dispara / Acción |
| :---: | :--- | :--- | :--- | :--- |
| **PUBLICA** | `styleradar.playlist.creada` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-playlist` $\rightarrow$ `ms-estadisticas`, suscriptores en `styleradar.exchange` | Se dispara cuando se crea una nueva playlist (`SR-RF-69`). |
| **PUBLICA** | `styleradar.playlist.prenda.asociada` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-playlist` $\rightarrow$ Suscriptores en `styleradar.exchange` | Se dispara cuando se asocia una prenda a una playlist (`SR-RF-70`). |
| **PUBLICA** | `styleradar.playlist.destacada.actualizada` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-playlist` $\rightarrow$ Suscriptores en `styleradar.exchange` | Se dispara cuando una playlist es marcada o desmarcada como destacada (`SR-RF-72`). |
| **CONSUME** | `styleradar.catalogo.prenda.retirada` | `styleradar.playlist.prenda-retirada.q` *(DLQ: `styleradar.playlist.dlq`)* | `ms-item-catalogo` $\rightarrow$ `ms-playlist` | Marca el `PrendaSnapshot` correspondiente como no disponible (`disponible = false`) en todas las playlists que contengan la prenda retirada. |

<details>
<summary><b>Click para ver Esquema JSON del Evento Publicado y Consumido</b></summary>

#### 1. Evento Publicado: `styleradar.playlist.creada`
```json
{
  "eventId": "evt-pl-creada-9101",
  "eventType": "styleradar.playlist.creada",
  "timestamp": "2026-11-24T16:15:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "8f14e45f-eaab-42b0-9f1a-1234567890ab"
  }
}
```

#### 2. Evento Publicado: `styleradar.playlist.prenda.asociada`
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "eventType": "styleradar.playlist.prenda.asociada",
  "timestamp": "2026-11-24T20:00:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "8f14e45f-eaab-42b0-9f1a-1234567890ab",
    "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4"
  }
}
```

#### 3. Evento Publicado: `styleradar.playlist.destacada.actualizada`
```json
{
  "eventId": "c9bf9e57-1685-4c89-bafb-ff5af830be8a",
  "eventType": "styleradar.playlist.destacada.actualizada",
  "timestamp": "2026-11-24T20:05:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "8f14e45f-eaab-42b0-9f1a-1234567890ab",
    "destacada": true
  }
}
```

#### 4. Evento Consumido: `styleradar.catalogo.prenda.retirada`
```json
{
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "eventType": "styleradar.catalogo.prenda.retirada",
  "timestamp": "2026-11-24T20:10:00Z",
  "producer": "ms-item-catalogo",
  "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4"
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
