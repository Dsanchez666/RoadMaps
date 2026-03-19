# Roadmap MVP Project

Proyecto con backend Java (arquitectura hexagonal) y frontend Angular.

## Ejecucion rapida

### Requisitos
- Java 17+
- Maven 3.8+
- Node.js 20.19+ LTS o 22.12+

### Rutas locales
- Backend: `C:\SideProjects\RoadMaps\backend`
- Frontend: `C:\SideProjects\RoadMaps\frontend`

### Backend (Spring Boot, puerto 8080)
```bash
cd C:\SideProjects\RoadMaps\backend
mvn clean install
mvn spring-boot:run
```

Nota: el backend incluye `backend/.mvn/maven.config` para fijar el repo local en `C:\SideProjects\RoadMaps\.m2repo` y evitar errores de permisos.

### Frontend Angular (puerto 4200)
```bash
cd C:\SideProjects\RoadMaps\frontend
npm install
npm run build
npm run start
```

### Puertos y proxy
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`
- Proxy de desarrollo: `frontend/proxy.conf.json` (redirige `/api` al backend)

## Estado actual
- Backend funcional con API REST.
- Frontend Angular activo en `frontend/src`.
- Proxy de desarrollo en `frontend/proxy.conf.json`.

## Estructura
- `backend/`: API y logica de negocio.
- `frontend/`: app Angular, scripts npm y configuracion CLI.
- `docs/`: documentacion funcional y tecnica.

## Configuracion de datos
- El repositorio de roadmaps usa fichero configurable con `roadmap.data.path`.
- Valor por defecto: `data/roadmaps.txt`.

## Tests
### Frontend
```bash
cd C:\SideProjects\RoadMaps\frontend
npm run build
npm run test:ci
npm audit
npm audit --omit=dev
```

### Backend
```bash
cd C:\SideProjects\RoadMaps\backend
mvn test
```

## Documentacion
- `QUICKSTART.md`
- `ARCHITECTURE.md`
- `docs/README_FRONTEND.md`
- `docs/DATABASE_CONFIGURATION.md`
- `docs/ORACLE_INTEGRATION.md`
