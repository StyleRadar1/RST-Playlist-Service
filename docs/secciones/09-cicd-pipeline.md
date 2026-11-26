[⬅ Volver al README Principal](../../README.md)

---

## 9. CI/CD Y PIPELINE AUTOMATIZADO

La automatización de integración y entrega continua de `ms-playlist` se ejecuta en **GitHub Actions** mediante dos workflows oficiales en `.github/workflows/`:

* **`ci.yml` (`CI`):** Compila el proyecto con JDK 21, ejecuta pruebas unitarias y de integración, verifica cobertura y análisis estático con `mvn -B -ntp verify`, publica el reporte `jacoco-report` (`target/site/jacoco/`) y valida la construcción de la imagen Docker.
* **`cd.yml` (`CD`):** Construye y publica la imagen OCI en **GitHub Container Registry (`ghcr.io`)**, asegura la base de datos en PostgreSQL Flexible Server (`playlist_db` / `playlist_db_prod`), despliega en **Azure Container Apps** (`styleradar-cae-prod`, región `northcentralus`), configura sondas *Startup* y *Readiness* sobre `/api/v1/health` y verifica que el servicio responda `200 OK`.

### Badges de Pipelines y Calidad (GitHub Actions)

<div align="center">
  <p>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml">
      <img src="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml/badge.svg?branch=develop" alt="CI Develop" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml">
      <img src="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml/badge.svg?branch=master" alt="CI Master" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml">
      <img src="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml/badge.svg" alt="CD Deploy" />
    </a>
  </p>
  <p>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml">
      <img src="https://img.shields.io/badge/CI-ci.yml-12063A?style=for-the-badge&logo=githubactions&logoColor=white" alt="Workflow CI" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml">
      <img src="https://img.shields.io/badge/CD-cd.yml-5600EF?style=for-the-badge&logo=githubactions&logoColor=white" alt="Workflow CD" />
    </a>
    <a href="../secciones/08-pruebas-y-cobertura.md">
      <img src="https://img.shields.io/badge/JaCoCo-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="JaCoCo 85%" />
    </a>
    <a href="../secciones/08-pruebas-y-cobertura.md">
      <img src="https://img.shields.io/badge/Registry-ghcr.io-12063A?style=for-the-badge&logo=docker&logoColor=C4B5FD" alt="GHCR Registry" />
    </a>
  </p>
</div>

### Matriz de Pipelines por Rama Protegida

| Pipeline | Rama / Entorno | Trigger de Ejecución | Quality Gate Requerido | Estado |
| :--- | :--- | :--- | :--- | :---: |
| **CI (`ci.yml`)** | `master`, `develop`, `feature/**` | `push`, `pull_request` (`master`, `develop`) y `workflow_dispatch` | Compilación Java 21 (`temurin`), `mvn -B -ntp verify` (pruebas, análisis estático y JaCoCo `≥ 85%`) y build Docker sin push | [![CI](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/ci.yml) |
| **CD QA (`cd.yml`)** | `develop` &rarr; **QA** (`rst-playlist-service-qa`) | `push` en `develop` o `workflow_dispatch` (`ambiente: qa`) | Imagen publicada en `ghcr.io`, BD `playlist_db`, RabbitMQ TLS `:5671`, `min-replicas 1` y verificación `/api/v1/health` | [![CD QA](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml) |
| **CD Producción (`cd.yml`)** | `master` &rarr; **PROD** (`rst-playlist-service-prod`) | `push` en `master` (con CMV) o `workflow_dispatch` (`ambiente: prod`) | Imagen publicada en `ghcr.io`, BD `playlist_db_prod`, `ITEM_CATALOGO_URL` PROD, `min-replicas 1` y verificación `/api/v1/health` | [![CD PROD](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml/badge.svg?branch=master)](https://github.com/StyleRadar1/RST-Playlist-Service/actions/workflows/cd.yml) |
---

[⬅ Volver al README Principal](../../README.md)
