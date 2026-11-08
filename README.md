<div align="center">

  <a href="https://github.com/StyleRadar1">
    <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="340" alt="StyleRadar Logo Oficial" />
  </a>

  <h1>STYLE<font color="#5600EF">RADAR</font> &nbsp;·&nbsp; <code>ms-playlist</code></h1>

  <p align="center">
    <img src="https://img.shields.io/badge/BOUNDED_CONTEXT-Playlists_de_estilo-12063A?style=for-the-badge" alt="Context" />
    <img src="https://img.shields.io/badge/PUERTO_OFICIAL-8260-5600EF?style=for-the-badge" alt="Port" />
    <img src="https://img.shields.io/badge/ARQUITECTURA-HEXAGONAL-16A34A?style=for-the-badge" alt="Hexagonal" />
  </p>

  <p align="center">
    <strong><font color="#5600EF">Descubre tu estilo. En tu ciudad.</font></strong><br />
    <em>Plataforma de moda local con inventario multi-sede geolocalizado en tiempo real y probador virtual para Bogotá.</em>
  </p>

  <p align="center">
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml">
      <img src="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml/badge.svg?branch=develop" alt="CI" />
    </a>
    <a href="#9-pruebas-y-cobertura">
      <img src="https://img.shields.io/badge/JaCoCo_Coverage-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="Coverage" />
    </a>
    <a href="#6-tecnologías-y-dependencias">
      <img src="https://img.shields.io/badge/Java-21_LTS-12063A?style=for-the-badge&logo=openjdk&logoColor=C4B5FD" alt="Java 21" />
    </a>
    <a href="#6-tecnologías-y-dependencias">
      <img src="https://img.shields.io/badge/Spring_Boot-3.3.4-12063A?style=for-the-badge&logo=spring&logoColor=C4B5FD" alt="Spring Boot 3.3.4" />
    </a>
    <a href="#7-cómo-ejecutar-local-y-con-docker">
      <img src="https://img.shields.io/badge/Docker-OCI_Container-12063A?style=for-the-badge&logo=docker&logoColor=C4B5FD" alt="Docker OCI" />
    </a>
  </p>

</div>


---

## <img src="https://img.shields.io/badge/ÍNDICE-5600EF?style=for-the-badge" height="20" align="absmiddle" /> ÍNDICE DE CONTENIDOS

1. [Nombre del servicio y su bounded context](#1-nombre-del-servicio-y-su-bounded-context)
2. [Equipo y responsables](#2-equipo-y-responsables)
3. [Arquitectura hexagonal con su diagrama](#3-arquitectura-hexagonal-con-su-diagrama)
4. [Endpoints y enlace a Swagger](#4-endpoints-y-enlace-a-swagger)
5. [Eventos que publica y consume](#5-eventos-que-publica-y-consume)
6. [Tecnologías y dependencias](#6-tecnologías-y-dependencias)
7. [Cómo ejecutar local y con Docker](#7-cómo-ejecutar-local-y-con-docker)
8. [Variables de entorno](#8-variables-de-entorno)
9. [Pruebas y cobertura](#9-pruebas-y-cobertura)
10. [CI/CD con el badge del pipeline](#10-cicd-con-el-badge-del-pipeline)
11. [Ramas y cómo contribuir](#11-ramas-y-cómo-contribuir)
12. [Enlace a la épica en Jira](#12-enlace-a-la-épica-en-jira)

---

<a id="1-nombre-del-servicio-y-su-bounded-context"></a>

## <img src="https://img.shields.io/badge/01-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `ms-playlist`
* **Paquete Arquitectónico:** `Paquete 2: Catálogo`
* **Puerto de Escucha Oficial:** `8260`
* **Base de Datos Asignada:** `PostgreSQL 15.8` (`styleradar_playlist`), aislada bajo el patrón *Database-per-Service*.
* **Bounded Context:** `Playlists de estilo`

### Responsabilidades del Dominio

Este microservicio encapsula exclusivamente la lógica de negocio de las playlists de estilo creadas por los usuarios compradores de StyleRadar.

| Qué HACE este Microservicio (Alcance) | Qué NO HACE este Microservicio (Límites) |
| :--- | :--- |
| • Crea y configura playlists públicas o privadas. | • NO autentica usuarios. |
| • Agrega y retira prendas de una playlist. | • NO publica ni edita prendas (delegado a `ms-item-catalogo` en `:8220`). |
| • Registra los Me gusta y las playlists guardadas. | • NO gestiona el inventario por talla (delegado a `ms-inventario` en `:8200`). |
| • Destaca las playlists con mayor interacción. | • NO accede directamente a bases de datos de otros microservicios. |

### Arquetipos de Moda y Personajes de StyleRadar

<div align="center">

| Antonia (Sastre) | Mateo (Calle) | Aura (Futuro) | Nora (Rebelde) | Tomás (Esencial) |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/antonia.jpg" width="110" style="border-radius:8px;" alt="Antonia" /><br>**Sastre**<br><sub>Tiendas & Descubrimiento</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/mateo.jpg" width="110" style="border-radius:8px;" alt="Mateo" /><br>**Calle**<br><sub>Playlists & Cultura Urbana</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/aura.jpg" width="110" style="border-radius:8px;" alt="Aura" /><br>**Futuro**<br><sub>Probador Virtual AI</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/nora.jpg" width="110" style="border-radius:8px;" alt="Nora" /><br>**Rebelde**<br><sub>Segunda Vida & Vintage</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/tomas.jpg" width="110" style="border-radius:8px;" alt="Tomás" /><br>**Esencial**<br><sub>Fundaciones & Donaciones</sub> |

</div>

---

<a id="2-equipo-y-responsables"></a>

## <img src="https://img.shields.io/badge/02-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 2. EQUIPO Y RESPONSABLES

Desarrollado y mantenido por el **Equipo `Restrepo`** (prefijo `RST`) para la asignatura DOSW 2026.

<div align="center">

| Rol | Nombre Completo | Usuario GitHub |
| :---: | :---: | :---: |
| **Líder y apoyo** | `Juan Nicolás Álvarez Muñoz` | [@JUANNICOLASA](https://github.com/JUANNICOLASA) |
| **Backend Developer** | `Daniel Mosquera` | [@Danielmmartinez](https://github.com/Danielmmartinez) |

</div>

---

<a id="3-arquitectura-hexagonal-con-su-diagrama"></a>

## <img src="https://img.shields.io/badge/03-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 3. ARQUITECTURA HEXAGONAL CON SU DIAGRAMA

El servicio sigue la arquitectura hexagonal acordada por el curso. Las reglas del negocio se mantienen aisladas de las herramientas externas, como la base de datos, el broker de mensajería o la API REST, de modo que estas pueden cambiar sin afectar la lógica principal.

### Diagrama Interactivo de Capas (Mermaid)

```mermaid
flowchart TD
    GW["API Gateway"]
    BRK["RabbitMQ"]

    subgraph INFRA_IN["Infraestructura de Entrada (Driving Adapters)"]
        REST["Adaptador REST"]
        AMQP_IN["Adaptador de eventos"]
    end

    subgraph APP["Capa de Aplicación"]
        IN_PORT["Puertos de entrada"]
        USE_CASES["Casos de uso"]
        OUT_PORT["Puertos de salida"]
    end

    subgraph DOMAIN["Núcleo de Dominio"]
        ENTITIES["Modelo de dominio"]
        EVENTS["Eventos de dominio"]
        EXCEPTIONS["Excepciones de dominio"]
    end

    subgraph INFRA_OUT["Infraestructura de Salida (Driven Adapters)"]
        JPA_REPO["Adaptador de persistencia"]
        RABBIT_PUB["Adaptador de mensajería"]
    end

    DB[("PostgreSQL")]

    GW --> REST
    BRK --> AMQP_IN
    REST --> IN_PORT
    AMQP_IN --> IN_PORT
    IN_PORT --> USE_CASES
    USE_CASES --> DOMAIN
    USE_CASES --> OUT_PORT
    OUT_PORT --> JPA_REPO
    OUT_PORT --> RABBIT_PUB
    JPA_REPO --> DB
    RABBIT_PUB --> BRK

    classDef domain fill:#EDE7FF,stroke:#5600EF,stroke-width:2px,color:#0C0C12;
    classDef app fill:#F8FAFC,stroke:#C4B5FD,stroke-width:2px,color:#0C0C12;
    classDef infra fill:#12063A,stroke:#5600EF,stroke-width:1.5px,color:#FFFFFF;

    class DOMAIN,ENTITIES,EVENTS,EXCEPTIONS domain;
    class APP,IN_PORT,USE_CASES,OUT_PORT app;
    class INFRA_IN,INFRA_OUT,REST,AMQP_IN,JPA_REPO,RABBIT_PUB infra;
```

### Diagrama de Componentes Específico

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #C4B5FD; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 20px;">
        <p>
          <img src="https://img.shields.io/badge/DIAGRAMA-COMPONENTES-12063A?style=for-the-badge" alt="Diagrama de componentes" /><br />
          <strong><font color="#5600EF" size="3">DIAGRAMA DE COMPONENTES ESPECÍFICO (HEXAGONAL)</font></strong>
        </p>
        <p>
          <img src="docs/evidencias/StyleRadar-ComponentesPlayList.png" width="100%" alt="Diagrama de componentes" /><br />
        </p>
      </td>
    </tr>
  </table>
</div>

El diagrama muestra el servicio en arquitectura hexagonal. Las peticiones llegan desde el API Gateway al controller REST de playlists y los eventos del catálogo llegan por RabbitMQ a `CatalogoEventsConsumer` (adaptadores de entrada), que solo conocen el caso de uso (puerto de entrada). La lógica de aplicación usa los puertos de salida para guardar las playlists en PostgreSQL (`PlaylistRepositoryPort`), publicar sus eventos en RabbitMQ (`PlaylistEventPublisherPort`) y mantener en Redis las playlists destacadas (`PlaylistCachePort`). Las dependencias apuntan hacia adentro, por lo que el dominio no conoce Spring, JPA ni HTTP.

### Diagrama de Clases

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #C4B5FD; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 20px;">
        <p>
          <img src="https://img.shields.io/badge/DIAGRAMA-CLASES-12063A?style=for-the-badge" alt="Diagrama de clases" /><br />
          <strong><font color="#5600EF" size="3">DIAGRAMA DE CLASES DEL DOMINIO</font></strong>
        </p>
        <p>
          <img src="docs/evidencias/StyleRadar-DiagramadeClasesPlaylist.drawio.png" width="100%" alt="Diagrama de clases" /><br />
        </p>
      </td>
    </tr>
  </table>
</div>

El agregado principal es `Playlist`, que guarda el nombre, la descripción, la `VisibilidadPlaylist` (pública o privada), el `EstiloMusical` y las prendas asociadas como `PrendaSnapshot`, una copia del nombre de la prenda tomada del catálogo. Los métodos del agregado cubren crear, destacar y quitar de destacadas (SR-RF-72), asociar y retirar prendas (SR-RF-70), y alternar el me gusta y el guardado de cada usuario (SR-RF-71). Antes de modificar o mostrar una playlist se valida que el usuario sea su dueño o que la playlist sea pública.

### Diagrama de Base de Datos

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #C4B5FD; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 20px;">
        <p>
          <img src="https://img.shields.io/badge/DIAGRAMA-BASE_DE_DATOS-12063A?style=for-the-badge" alt="Diagrama de base de datos" /><br />
          <strong><font color="#5600EF" size="3">MODELO DE DATOS (POSTGRESQL)</font></strong>
        </p>
        <p>
          <img src="docs/evidencias/StyleRadar-DiagramaEventoRelacionPlaylist.drawio.png" width="100%" alt="Diagrama de base de datos" /><br />
        </p>
      </td>
    </tr>
  </table>
</div>

Se usa PostgreSQL porque las playlists, sus prendas y las interacciones de los usuarios tienen relaciones fijas. La tabla `playlist` guarda los datos de cada playlist y si está destacada; `playlist_prendas` guarda las prendas asociadas, y `playlist_likes` y `playlist_guardados` registran qué usuarios dieron me gusta o guardaron cada playlist, todas en relación uno a muchos con `playlist`. Las prendas y los usuarios se guardan solo por su identificador, ya que pertenecen a otros servicios (*Database-per-Service*).

### Estructura Oficial de Paquetes en `src/main/java/`

```text
src/main/java/edu/dosw/styleradar/playlist/
├── domain/
│   ├── model/                       <-- Entidades y agregados
│   ├── event/                       <-- Eventos de dominio
│   ├── exception/                   <-- Excepciones de negocio
│   └── port/
│       ├── in/                      <-- Puertos de entrada (casos de uso)
│       └── out/                     <-- Puertos de salida (repositorios, publicadores)
├── application/
│   ├── usecase/                     <-- Implementación de casos de uso (Impl)
│   ├── service/                     <-- Servicios de aplicación
│   │   └── impl/
│   ├── dto/
│   │   ├── request/
│   │   ├── response/
│   │   └── event/
│   └── mapper/
│       ├── in/
│       └── out/
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── rest/                <-- Controllers REST
    │   │   │   └── doc/             <-- Interfaces de documentación OpenAPI
    │   │   └── amqp/                <-- Consumidores RabbitMQ
    │   └── out/
    │       ├── persistence/         <-- Entidades JPA y repositorios
    │       │   ├── entity/
    │       │   └── repository/
    │       ├── messaging/           <-- Publicadores RabbitMQ
    │       ├── external/            <-- Clientes de otros servicios
    │       └── security/
    ├── config/                      <-- Configuración Spring y OpenAPI
    └── exception/                   <-- Manejo global de errores (RFC 7807)
```

---

<a id="4-endpoints-y-enlace-a-swagger"></a>

## <img src="https://img.shields.io/badge/04-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 4. ENDPOINTS Y ENLACE A SWAGGER

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | Propósito | URL Base del Servicio | Swagger OpenAPI UI | Health Check |
| :--- | :---: | :--- | :--- | :--- | :--- |
| **Producción (PROD)** | Próximo Sprint | Despliegue productivo de StyleRadar | Próximo Sprint | Próximo Sprint | Próximo Sprint |
| **Calidad / Pruebas (QA)** | Próximo Sprint | Pruebas integradas del Sprint | Próximo Sprint | Próximo Sprint | Próximo Sprint |
| **Desarrollo Local (DEV)** | Local | Entorno de desarrollo local | `http://localhost:8260/api/v1` | [Swagger UI Local](http://localhost:8260/api/v1/swagger-ui.html) | [`/health`](http://localhost:8260/api/v1/health) |

* **Especificación OpenAPI:** `http://localhost:8260/api/v1/v3/api-docs`

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_01-5600EF?style=for-the-badge" alt="Evidencia 01" /><br />
          <strong><font color="#5600EF" size="3">DOCUMENTACIÓN SWAGGER OPENAPI UI ACTIVA</font></strong>
        </p>
        <p>
<em>Evidencia pendiente.</em>
        </p>
      </td>
    </tr>
  </table>
</div>

### Tabla de Endpoints Expuestos

| Método | Endpoint | Descripción | Rol Requerido | Códigos HTTP |
| :---: | :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/playlists` | Crear una playlist | Autenticado | `201`, `400` |
| `GET` | `/api/v1/playlists` | Consultar las playlists públicas | Público | `200` |
| `GET` | `/api/v1/playlists/{id}` | Consultar el detalle de una playlist | Público / Autenticado | `200`, `403`, `404` |
| `GET` | `/api/v1/playlists/destacadas` | Consultar las playlists destacadas (SR-RF-72) | Público | `200` |
| `GET` | `/api/v1/playlists/populares` | Consultar las playlists más populares, paginadas | Público | `200` |
| `GET` | `/api/v1/playlists/me` | Consultar las playlists creadas por el usuario | Autenticado | `200` |
| `GET` | `/api/v1/playlists/me/guardadas` | Consultar las playlists guardadas por el usuario | Autenticado | `200` |
| `PUT` | `/api/v1/playlists/{id}` | Editar una playlist propia (SR-RF-69) | Dueño | `200`, `400`, `403` |
| `DELETE` | `/api/v1/playlists/{id}` | Eliminar una playlist propia | Dueño | `204`, `403`, `404` |
| `PUT` | `/api/v1/playlists/{id}/destacar` | Destacar una playlist (SR-RF-72) | `ADMIN` | `200`, `409` |
| `DELETE` | `/api/v1/playlists/{id}/destacar` | Quitar una playlist de destacadas | `ADMIN` | `200` |
| `POST` | `/api/v1/playlists/{id}/prendas` | Agregar una prenda a la playlist (SR-RF-70) | Dueño | `200`, `404`, `422` |
| `DELETE` | `/api/v1/playlists/{id}/prendas/{prendaId}` | Retirar una prenda de la playlist (SR-RF-70) | Dueño | `200`, `404` |
| `POST` | `/api/v1/playlists/{id}/like` | Dar o quitar me gusta (SR-RF-71) | Autenticado | `200`, `404` |
| `POST` | `/api/v1/playlists/{id}/guardar` | Guardar o quitar de guardadas (SR-RF-71) | Autenticado | `200`, `404` |
| `GET` | `/api/v1/health` | Estado del servicio | Público | `200` |

---

<a id="5-eventos-que-publica-y-consume"></a>

## <img src="https://img.shields.io/badge/05-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 5. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona entre microservicios se realiza a través de **RabbitMQ 3.13.7**.

### Matriz de Eventos AMQP

| Tipo | Evento | Emisor / Receptor | Propósito |
| :---: | :--- | :--- | :--- |
| **CONSUME** | `StockChangedEvent` | `ms-inventario` &rarr; `ms-playlist` | Actualiza la disponibilidad de las prendas de la playlist. |
| **CONSUME** | `PrendaAgotadaEvent` | `ms-inventario` &rarr; `ms-playlist` | Marca la prenda como agotada sin retirarla de la playlist. |

---

<a id="6-tecnologías-y-dependencias"></a>

## <img src="https://img.shields.io/badge/06-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 6. TECNOLOGÍAS Y DEPENDENCIAS

| Capa / Componente | Tecnología | Versión |
| :--- | :--- | :--- |
| **Lenguaje Core** | Java | `21` |
| **Framework** | Spring Boot | `3.3.4` |
| **Base de Datos** | PostgreSQL | `15.8` |
| **Migraciones de BD** | Flyway | Gestionada por Spring Boot |
| **Broker de Mensajería** | RabbitMQ | `3.13.7` |
| **Mapeo de DTOs** | MapStruct | `1.5.5.Final` |
| **Documentación API** | springdoc-openapi | `2.6.0` |
| **Testing Integración** | Testcontainers | Gestionada por Spring Boot |
| **Métricas de Cobertura** | JaCoCo | `0.8.12` |
| **Análisis Estático** | SpotBugs | `4.8.6.4` |

---

<a id="7-cómo-ejecutar-local-y-con-docker"></a>

## <img src="https://img.shields.io/badge/07-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 7. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **JDK:** 21
* **Apache Maven:** 3.9+
* **Docker Engine y Docker Compose**

### Ejecución Local con Maven (Perfil Dev)

Requiere PostgreSQL y RabbitMQ disponibles en las direcciones definidas en el archivo `.env`, creado a partir de `.env.example`.

```bash
git clone https://github.com/StyleRadar1/RST-Playlist-Service.git
cd RST-Playlist-Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ejecución Contenerizada Completa con Docker

El servicio, junto con su base de datos y RabbitMQ, se levanta con Docker a partir del archivo `.env`:

```bash
docker compose up -d --build
```

### Comprobación de Salud (Health Check)

```bash
curl http://localhost:8260/api/v1/health
```

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_02-5600EF?style=for-the-badge" alt="Evidencia 02" /><br />
          <strong><font color="#5600EF" size="3">CONTENEDOR OCI DOCKER EN EJECUCIÓN (docker compose ps)</font></strong>
        </p>
        <p>
<em>Evidencia pendiente.</em>
        </p>
      </td>
    </tr>
  </table>
</div>

---

<a id="8-variables-de-entorno"></a>

## <img src="https://img.shields.io/badge/08-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 8. VARIABLES DE ENTORNO

Las variables se definen en `.env`, que no se versiona. La plantilla `.env.example` indica cuáles se requieren.

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SERVER_PORT` | Puerto del servicio | `8260` |
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `styleradar_playlist` |
| `DB_USER` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | — |
| `RABBITMQ_HOST` | Host de RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | `5672` |
| `RABBITMQ_USER` | Usuario de RabbitMQ | `styleradar` |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | — |

---

<a id="9-pruebas-y-cobertura"></a>

## <img src="https://img.shields.io/badge/09-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 9. PRUEBAS Y COBERTURA (≥ 85%)

Las pruebas unitarias validan el dominio y los casos de uso, y las de integración validan la persistencia y la mensajería. Se exige una cobertura mínima del **85 %**, verificada con **JaCoCo** en cada ejecución del pipeline.

### Comandos de Ejecución de Pruebas

```bash
./mvnw clean verify
```

Este comando ejecuta las pruebas, genera el reporte de cobertura y verifica el mínimo del 85 %.

* **Ruta del Reporte HTML:** `target/site/jacoco/index.html`

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_03-5600EF?style=for-the-badge" alt="Evidencia 03" /><br />
          <strong><font color="#5600EF" size="3">REPORTE CONSOLIDADO DE COBERTURA JACOCO (≥ 85%)</font></strong>
        </p>
        <p>
<em>Evidencia pendiente.</em>
        </p>
      </td>
    </tr>
  </table>
</div>

### Pirámide de Pruebas Implementada

| Nivel de Prueba | Alcance | Tecnologías | Ubicación en el Código |
| :--- | :--- | :--- | :--- |
| **Pruebas Unitarias** | Dominio y casos de uso | JUnit 5, Mockito | `src/test/java/.../domain/`, `src/test/java/.../application/` |
| **Pruebas de Integración** | Persistencia, mensajería y REST | Spring Boot Test, Testcontainers | `src/test/java/.../infrastructure/` |

---

<a id="10-cicd-con-el-badge-del-pipeline"></a>

## <img src="https://img.shields.io/badge/10-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 10. CI/CD CON EL BADGE DEL PIPELINE

[![CI](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml)

Cada cambio enviado a `master`, `develop` o a una rama `feature/*`, así como cada pull request, pasa por un pipeline de **GitHub Actions** que compila el proyecto, ejecuta las pruebas, verifica la cobertura, realiza el análisis estático del código y construye la imagen Docker.

### Matriz de Pipelines por Rama Protegida

| Rama | Desencadenador | Etapas |
| :--- | :--- | :--- |
| `develop` | Push y pull request | Compilación, pruebas, cobertura, análisis estático e imagen Docker |
| `master` | Push y pull request | Compilación, pruebas, cobertura, análisis estático e imagen Docker |

### Diagrama del Flujo Automatizado (Pipeline Stages)

```mermaid
flowchart LR
    A["Push / PR<br>(GitHub)"] --> B["Compilar<br>(Maven, Java 21)"]
    B --> C["Pruebas y JaCoCo<br>(cobertura ≥ 85%)"]
    C --> D["Análisis estático<br>(SpotBugs)"]
    D --> E["Imagen Docker"]

    classDef step fill:#12063A,stroke:#5600EF,stroke-width:2px,color:#FFFFFF;
    classDef pass fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#0C0C12;

    class A,B,E step;
    class C,D pass;
```

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_04-5600EF?style=for-the-badge" alt="Evidencia 04" /><br />
          <strong><font color="#5600EF" size="3">EJECUCIÓN DEL PIPELINE CI/CD EN VERDE</font></strong>
        </p>
        <p>
<em>Evidencia pendiente.</em>
        </p>
      </td>
    </tr>
  </table>
</div>

---

<a id="11-ramas-y-cómo-contribuir"></a>

## <img src="https://img.shields.io/badge/11-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 11. RAMAS Y CÓMO CONTRIBUIR

### Estructura de Ramas Oficial

```text
master                              <-- Versiones estables
└── develop                         <-- Integración del trabajo del equipo
    └── feature/HU-xx-descripcion   <-- Una rama por historia de usuario
```

Los cambios llegan a `develop` y `master` únicamente por pull request, con revisión y el pipeline en verde.

### Convención de Commits (*Conventional Commits*)

Todo mensaje de commit sigue el estándar:

```text
<tipo>(<alcance>): <descripción en español>
```

Los tipos más usados son `feat`, `fix`, `test`, `docs`, `refactor` y `chore`.

### Checklist Obligatorio para Pull Requests

- [ ] La rama parte de la versión más reciente de `develop`.
- [ ] Todas las pruebas pasan y la cobertura JaCoCo es mayor o igual al 85 %.
- [ ] No se introducen credenciales ni tokens en el código fuente.
- [ ] El PR tiene la aprobación de otro integrante del equipo antes del merge.

---

<a id="12-enlace-a-la-épica-en-jira"></a>

## <img src="https://img.shields.io/badge/12-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 12. ENLACE A LA ÉPICA EN JIRA

### Enlaces de Gestión, Repositorios y Evidencias

* **Épica del Microservicio:** [RES-4 · ms-playlist](https://mail-team-nyjtgqcj.atlassian.net/browse/RES-4)
* **Tablero del Equipo:** [Restrepo-StyleRadar (RES)](https://mail-team-nyjtgqcj.atlassian.net/jira/software/projects/RES/boards/76)
* **Documento Técnico del Servicio:** [Documento técnico de ms-playlist](docs/DocumentoTecnicoPlaylist.html)

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_05-5600EF?style=for-the-badge" alt="Evidencia 05" /><br />
          <strong><font color="#5600EF" size="3">TABLERO SCRUM Y SEGUIMIENTO DE LA ÉPICA EN JIRA</font></strong>
        </p>
        <p>
<em>Evidencia pendiente.</em>
        </p>
      </td>
    </tr>
  </table>
</div>

### Requerimientos Funcionales Asignados

| Código RF | Nombre del Requerimiento | Historia en Jira |
| :---: | :--- | :---: |
| `SR-RF-69` | Crear y configurar playlist | `RES-17` |
| `SR-RF-70` | Agregar o retirar prendas de playlist | `RES-18` |
| `SR-RF-71` | Interactuar con playlist | `RES-19` |
| `SR-RF-72` | Destacar playlists populares | `RES-20` |
