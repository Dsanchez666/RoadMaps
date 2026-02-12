# ⚡ Inicio Rápido

## 🎯 Ejecutar ambas aplicaciones (2 terminales)

### Terminal 1 - Backend (puerto 8081)
```bash
cd roadmap-mvp-project\backend\src\main\java\com\example\roadmap
javac SimpleServer.java
java -cp ../../.. com.example.roadmap.SimpleServer
```

### Terminal 2 - Frontend (puerto 3000)
```bash
cd roadmap-mvp-project\frontend
javac FrontendServer.java
java FrontendServer
```

---

## 📍 Acceder a las aplicaciones

| Aplicación | URL | Función |
|---|---|---|
| **Frontend** | http://localhost:3000 | UI para crear/listar roadmaps |
| **API Backend** | http://localhost:8081/api/roadmaps | REST API |

---

## 🧪 Pruebas manuales (PowerShell/cmd)

### Crear roadmap:
```powershell
$body = @{ title='Mi Roadmap'; description='Primera versión' } | ConvertTo-Json
Invoke-WebRequest -Uri http://localhost:8081/api/roadmaps -Method Post -Body $body -ContentType 'application/json'
```

### Listar roadmaps:
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/roadmaps -Method Get
```

---

## 📋 Tecnologías utilizadas

| Componente | Tecnología | Versión |
|---|---|---|
| Backend | Java | 17+ |
| Frontend | HTML5 + JS | ES6+ |
| Servidor Backend | HttpServer (JDK) | - |
| Servidor Frontend | HttpServer (JDK) | - |
| Persistencia | En memoria | MVP |

---

## 💡 Notas

- ✅ **Sin dependencias externas:** Solo JDK 17+ (no requiere Maven, npm, npm, etc.)
- 📦 **Arquitectura hexagonal:** Limpia separación de capas en backend
- 🎨 **Frontend responsivo:** CSS3 moderno sin frameworks
- 📝 **Documentación completa:** requisitos, análisis, diseño en `/docs`
