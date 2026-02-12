# 🏛️ Arquitectura Hexagonal (Puertos y Adaptadores)

## Concepto

La arquitectura hexagonal aísla la lógica de negocio del resto de la aplicación, permitiendo:
- ✅ Cambiar adaptadores (Web, BD, etc.) sin afectar el dominio
- ✅ Testear casos de uso independientemente
- ✅ Reutilizar lógica en múltiples contextos

```

┌─────────────────────────────────────────────────────┐
│  Adaptadores Externos (Web, BD, etc.)              │
├─────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────┐ │
│  │    Puertos (Interfaces)                      │ │
│  │  ┌────────────────────────────────────────┐  │ │
│  │  │  DOMINIO (Lógica pura)                │  │ │
│  │  │  ├─ Entidades (Roadmap)               │  │ │
│  │  │  └─ Reglas de negocio                 │  │ │
│  │  └────────────────────────────────────────┘  │ │
│  │  ┌────────────────────────────────────────┐  │ │
│  │  │  APLICACION (Casos de Uso)            │  │ │
│  │  │  └─ CreateRoadmapUseCase              │  │ │
│  │  └────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│  Adaptadores IN (Entrada): REST, CLI              │
│  Adaptadores OUT (Salida): BD, Email               │
└─────────────────────────────────────────────────────┘

```

## Implementación en el Proyecto

### 1️⃣ **DOMAIN Layer** (com.example.roadmap.domain)

```java
// Entidad del dominio - Lógica de negocio 100% pura
Roadmap.java
  - id: String
  - title: String  
  - description: String
  - createdAt: Instant

// Puerto (interfaz) - Contrato con el exterior
RoadmapRepository.java
  - save(Roadmap): Roadmap
  - findById(String id): Optional<Roadmap>
  - findAll(): List<Roadmap>
```

**Característica clave:** El dominio NO importa nada del framework o adaptadores.

---

### 2️⃣ **APPLICATION Layer** (com.example.roadmap.application)

```java
// Caso de uso - Orquestalogra interacciones entre entidades y puertos
CreateRoadmapUseCase.java
  - constructor(RoadmapRepository) // Inyección del puerto
  - create(title, description): Roadmap
  - getById(id): Optional<Roadmap>
  - list(): List<Roadmap>
```

**Característica clave:** Opera solo con interfaces (puertos), no implementaciones concretas.

---

### 3️⃣ **ADAPTERS IN** (com.example.roadmap.adapters.in.web)

```java
// Adaptador REST - Entrada de solicitudes HTTP
RoadmapController.java
  - POST /api/roadmaps      → create()
  - GET  /api/roadmaps      → list()
  - GET  /api/roadmaps/{id} → getById()
```

**Características:**
- Traduce HTTP a llamadas de caso de uso
- Instancia el caso de uso (inyección manual o Spring)
- Serializa respuestas a JSON

---

### 4️⃣ **ADAPTERS OUT** (com.example.roadmap.adapters.out.persistence)

```java
// Adaptador de persistencia - Salida para guardar datos
InMemoryRoadmapRepository.java
  - Implementa RoadmapRepository (el puerto)
  - Almacena en Map<String, Roadmap> en memoria
  - Sustituible: JPA, MongoDB, ficheros, etc.
```

**Cambio fácil:** Reemplazar `InMemoryRoadmapRepository` por `JpaRoadmapRepository` sin tocar dominio ni aplicación.

---

## Flujo de una solicitud

```
Cliente HTTP
     ↓
[1] RoadmapController (Adapter IN)
     ↓
[2] CreateRoadmapUseCase (Application)
     ↓
[3] Roadmap (Domain)
     ↓
[4] RoadmapRepository (Puerto - interfaz)
     ↓
[5] InMemoryRoadmapRepository (Adapter OUT)
     ↓
Respuesta JSON
```

**Lo importante:** Si cambias del adaptador [5], el flujo 1-4 permanece igual.

---

## Ventajas en este proyecto

| Aspecto | Beneficio |
|--------|-----------|
| **Testabilidad** | Tests unitarios de `CreateRoadmapUseCase` sin HTTP/BD |
| **Escalabilidad** | Añadir nuevos adaptadores (GraphQL, AMQP) sin cambios |
| **Mantenibilidad** | Lógica de negocio centralizada y aislada |
| **Independencia** | No acoplado a Spring, JPA ni ningún framework |

---

## Extensiones futuras (sin cambiar lo existente)

```java
// Agregar JPA
@Repository
public class JpaRoadmapRepository implements RoadmapRepository { ... }

// Agregar GraphQL
@Component
public class RoadmapGraphQLAdapterIN { ... }

// Agregar eventos
public class RoadmapCreatedEvent { ... }
```

Todos ellos funcionarán con `CreateRoadmapUseCase` sin modificación.

---

## Suma ry

✅ **Este proyecto implementa Clean Architecture** mediante:
1. Dominio desacoplado (sin dependencias externas)
2. Casos de uso como orquestadores
3. Interfaces (puertos) hacia adaptadores
4. Múltiples adaptadores (es fácil cambiarlos)

Es el punto de partida perfecto para evolucionara arquitectura en producción.
