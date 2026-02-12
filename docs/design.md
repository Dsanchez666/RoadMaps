# Diseño - Roadmap MVP

## Estructura (hexagonal)
- Domain: entidades y contratos (interfaces) - `com.example.roadmap.domain`
- Application: casos de uso que orquestan el dominio - `com.example.roadmap.application`
- Adapters In: controladores REST - `com.example.roadmap.adapters.in` 
- Adapters Out: implementaciones de persistencia - `com.example.roadmap.adapters.out`

## API REST
- `POST /api/roadmaps` -> crear roadmap (payload: title, description)
- `GET /api/roadmaps` -> listar
- `GET /api/roadmaps/{id}` -> obtener

## Extensibilidad
- Reemplazar `InMemoryRoadmapRepository` por una implementación JPA para persistencia real.
- Añadir validaciones y DTOs más ricos.

## Pruebas
- Unit tests de casos de uso en `backend/src/test/java`.
- Integración futura con Spring Boot Test para endpoints.
