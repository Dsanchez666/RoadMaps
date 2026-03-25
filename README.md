# Roadmap MVP Project

Proyecto con backend Java (arquitectura hexagonal) y frontend Angular.

## Ejecucion rapida

### Requisitos
- Java 17+
- Maven 3.8+
- Node.js 20.19+ LTS o 22.12+

### Backend (Spring Boot, puerto 8080)
```bash
cd roadmap-mvp-project/backend
mvn clean install
mvn spring-boot:run
```

### Migracion MySQL requerida (iniciativas dinamicas)
Para BBDD existentes, ejecuta antes:
```bash
mysql -u roadmap -p roadmap_mvp < Database/migrations/2026-03-25_iniciativas_informacion_adicional.sql
```
La conexion MySQL falla de forma controlada si no existe `iniciativas.informacion_adicional`.

### Frontend Angular (puerto 4200)
```bash
cd roadmap-mvp-project/frontend
npm install
npm run start
```

## Estado actual
- Backend funcional con API REST.
- Frontend Angular activo en `frontend/src`.
- Proxy de desarrollo en `frontend/proxy.conf.json`.
- Sin servidores Java legacy de frontend/backend.

## Estructura
- `backend/`: API y logica de negocio.
- `frontend/`: app Angular, scripts npm y configuracion CLI.
- `docs/`: documentacion funcional y tecnica.

## Test y calidad
### Frontend
```bash
cd roadmap-mvp-project/frontend
npm run build
npm run test:ci
npm audit
npm audit --omit=dev
```

### Backend
```bash
cd roadmap-mvp-project/backend
mvn test
```

## Documentacion
- `QUICKSTART.md`
- `ARCHITECTURE.md`
- `docs/README_FRONTEND.md`
- `docs/DATABASE_CONFIGURATION.md`
- `docs/ORACLE_INTEGRATION.md`
