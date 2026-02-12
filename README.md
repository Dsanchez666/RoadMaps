# Roadmap MVP Project

Proyecto completo con backend (Java + arquitectura hexagonal) y frontend (HTML + JavaScript).

## 🚀 Ejecución rápida

### Backend (puerto 8081)
```bash
cd roadmap-mvp-project/backend/src/main/java/com/example/roadmap
javac SimpleServer.java
java -cp ../../.. com.example.roadmap.SimpleServer
```
API disponible: **http://localhost:8081/api/roadmaps**

### Frontend (puerto 3000)
```bash
cd roadmap-mvp-project/frontend
javac FrontendServer.java
java FrontendServer
```
Interfaz disponible: **http://localhost:3000**

---

## 📁 Estructura del proyecto

```
roadmap-mvp-project/
├── backend/                              # Java backend con arquit. hexagonal
│   ├── src/main/java/com/example/roadmap/
│   │   ├── SimpleServer.java            # Servidor HTTP REST (sin Spring)
│   │   ├── domain/                      # Entidades y contratos
│   │   │   ├── Roadmap.java
│   │   │   └── RoadmapRepository.java
│   │   ├── application/                 # Casos de uso
│   │   │   └── CreateRoadmapUseCase.java
│   │   └── adapters/
│   │       ├── in/web/                  # Controladores REST
│   │       │   └── RoadmapController.java
│   │       └── out/persistence/         # Implementaciones de repos
│   │           └── InMemoryRoadmapRepository.java
│   └── src/test/                        # Tests unitarios
│
├── frontend/                             # Frontend HTML + JS puro
│   ├── FrontendServer.java              # Servidor estático
│   ├── index.html                       # UI completa con fetch API
│   └── src/                             # Archivos Angular (opcional)
│
└── docs/                                # Documentación
    ├── requirements.md                  # Requisitos funcionales
    ├── analysis.md                      # Análisis de decisiones
    ├── design.md                        # Diseño de arquitectura
    └── README_FRONTEND.md               # Guía frontend Angular
```

---

## 📋 Funcionalidades

**API REST:**
- `POST /api/roadmaps`        → Crear roadmap (title, description)
- `GET /api/roadmaps`         → Listar todos
- `GET /api/roadmaps/{id}`    → Obtener por ID

**Frontend:**
- Formulario para crear roadmaps
- Lista dinámica de roadmaps guardados
- Interfaz responsive y limpia

---

## 🏗️ Arquitectura

**Backend (Arquitectura Hexagonal):**
- **Domain:** Lógica pura (entidades, interfaces)
- **Application:** Casos de uso orquestadores
- **Adapters In:** Controladores REST
- **Adapters Out:** Implementaciones de persistencia (en memoria, sustituible por JPA)

**Frontend (HTML + JavaScript):**
- SPA mínima sin dependencias externas
- Fetch API para comunicación REST
- Estilos CSS3 integrados

---

## 📚 Documentación

- [Requisitos](docs/requirements.md) - RF, RNF, alcance
- [Análisis](docs/analysis.md) - Decisiones clave y contexto
- [Diseño](docs/design.md) - Estructura hexagonal y extensibilidad
- [Frontend Angular](docs/README_FRONTEND.md) - Guía para app Angular completa

---

## 🔧 Próximos pasos (opcional)

1. **Persistencia real:** Reemplazar `InMemoryRoadmapRepository` con JPA/Hibernate
2. **Frontend Angular:** Generar con `ng new` y seguir [README_FRONTEND.md](docs/README_FRONTEND.md)
3. **Tests integración:** Añadir Spring Boot Test para endpoints REST
4. **Docker:** Crear Dockerfile para backend y frontend
5. **CI/CD:** Integr GitHub Actions / Jenkins para pipelines

---

## ✅ Estado actual (MVP)

- ✅ Backend funcional (Java puro, sin dependencias Maven)
- ✅ Frontend funcional (HTML + JS, sin build tools)
- ✅ API REST completamente operativa
- ✅ Documentación técnica completa
- ✅ Prueba manual en http://localhost:3000

Puedes comenzar a crear y listar roadmaps inmediatamente.
