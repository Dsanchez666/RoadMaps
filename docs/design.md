# Diseño - Roadmap MVP

## Estructura (hexagonal)
- Domain: entidades y contratos (interfaces) - `com.example.roadmap.domain`
- Application: casos de uso que orquestan el dominio - `com.example.roadmap.application`
- Infrastructure: adaptadores REST y persistencia - `com.example.roadmap.infrastructure.adapter`
  - Web (in): `com.example.roadmap.infrastructure.adapter.in.web`
  - Persistencia (out): `com.example.roadmap.infrastructure.adapter.out.persistence`

## API REST
- `POST /api/roadmaps` -> crear roadmap (payload: title, description)
- `GET /api/roadmaps` -> listar
- `GET /api/roadmaps/{id}` -> obtener

## Extensibilidad
- Reemplazar `FileRoadmapRepository` por una implementación JPA para persistencia real.
- Añadir validaciones y DTOs más ricos.

## Pruebas
- Unit tests de casos de uso en `backend/src/test/java`.
- Integración futura con Spring Boot Test para endpoints.
