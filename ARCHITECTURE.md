# Arquitectura Hexagonal (Puertos y Adaptadores)

## Resumen

La solución está organizada en capas estrictas con dependencia hacia el centro:

1. **Domain**: modelo y contratos (puertos) sin dependencias externas.
2. **Application**: casos de uso y orquestación; depende solo de Domain.
3. **Infrastructure**: adaptadores de entrada/salida y configuración; depende de Application y Domain.

## Estructura de paquetes (Backend)

```
backend/src/main/java/com/example/roadmap
├─ domain
│  ├─ model
│  │  └─ Roadmap.java
│  └─ port
│     ├─ in
│     │  ├─ RoadmapCommandPort.java
│     │  └─ RoadmapQueryPort.java
│     └─ out
│        └─ RoadmapRepositoryPort.java
├─ application
│  ├─ usecase
│  │  └─ RoadmapUseCaseService.java
│  ├─ command
│  │  └─ CreateRoadmapCommand.java
│  └─ query
│     ├─ GetRoadmapQuery.java
│     └─ ListRoadmapsQuery.java
└─ infrastructure
   ├─ adapter
   │  ├─ in
   │  │  └─ web
   │  │     ├─ RoadmapController.java
   │  │     ├─ DatabaseConfigController.java
   │  │     ├─ dto
   │  │     │  ├─ RoadmapCreateRequest.java
   │  │     │  ├─ RoadmapResponse.java
   │  │     │  └─ Database*Response.java
   │  │     └─ mapper
   │  │        └─ RoadmapWebMapper.java
   │  └─ out
   │     └─ persistence
   │        ├─ InMemoryRoadmapRepository.java
   │        └─ FileRoadmapRepository.java
   ├─ config
   │  └─ RoadmapConfig.java
   └─ db
      ├─ DatabaseConnection.java
      ├─ MySQLConnection.java
      └─ DatabaseConnectionExample.java
```

## Reglas de dependencia

- **Domain** no depende de nada.
- **Application** depende solo de **Domain**.
- **Infrastructure** depende de **Application** y **Domain**.

## DTOs y mappers

- Los controladores REST nunca exponen entidades de dominio.
- Se usan DTOs en `infrastructure/adapter/in/web/dto`.
- El mapeo dominio → DTO se centraliza en `RoadmapWebMapper`.

## Validación

- La validación de entrada se aplica en los DTOs con Bean Validation.

## Persistencia

- `RoadmapRepositoryPort` define el contrato en Domain.
- `FileRoadmapRepository` implementa el puerto de salida con fichero configurable (`roadmap.data.path`).
- `InMemoryRoadmapRepository` queda disponible para pruebas o ejecuciones efímeras.

## Frontend (Angular)

```
frontend/src/app
├─ features
│  ├─ roadmaps
│  │  ├─ components
│  │  ├─ services
│  │  └─ utils
│  └─ database
│     ├─ components
│     └─ services
└─ shared
   └─ models
```

- Los modelos compartidos están en `shared/models`.
- La lógica de parsing/import se extrae a `features/roadmaps/utils`.

## Tests

- Unit tests en `backend/src/test/java` para casos de uso y persistencia.

## Validacion de arquitectura (ArchUnit)

Las reglas se ejecutan en tests y bloquean builds cuando hay incumplimientos.

**Ubicacion de las reglas**
- `backend/src/test/java/com/example/roadmap/architecture/HexagonalArchitectureTest.java`

**Limitaciones activas**
1. **Domain no depende de Application/Infrastructure**
   - Paquetes afectados: `com.example.roadmap.domain..`
2. **Application no depende de Infrastructure**
   - Paquetes afectados: `com.example.roadmap.application..`
3. **Domain no puede depender de frameworks**
   - Bloquea dependencias a `org.springframework..`, `jakarta..`, `javax..`, `com.fasterxml..` desde Domain.
4. **Application no puede depender de frameworks**
   - Bloquea dependencias a `org.springframework..`, `jakarta..`, `javax..`, `com.fasterxml..` desde Application.
5. **Adapters deben vivir en Infrastructure**
   - Cualquier paquete con `..adapters..` debe residir en `com.example.roadmap.infrastructure..`.
6. **Controllers solo en Web Adapter**
   - Clases `*Controller` deben residir en `com.example.roadmap.infrastructure.adapter.in.web..`.
7. **Config solo en Infrastructure**
   - Clases `*Config` deben residir en `com.example.roadmap.infrastructure..`.
8. **Puertos solo en `domain/port/in` y `domain/port/out`**
   - Clases `*Port` dentro de `domain` deben estar en esos paquetes.
9. **Application no referencia Web Adapter**
   - Prohibe dependencia a `com.example.roadmap.infrastructure.adapter.in.web..` desde Application.
10. **Web Adapter no referencia Persistencia**
    - Prohibe dependencia a `com.example.roadmap.infrastructure.adapter.out.persistence..` desde Web.
11. **`java.sql` solo en `infrastructure.db`**
    - Fuera de `com.example.roadmap.infrastructure.db..` no se permite `java.sql..`.
12. **DTOs solo en `infrastructure.adapter.in.web.dto`**
    - Clases `*Request` y `*Response` deben residir en `com.example.roadmap.infrastructure.adapter.in.web.dto..`.

**Como lanzar el test**
```bash
cd backend
mvn test
```
