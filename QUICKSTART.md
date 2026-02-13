# ⚡ Inicio Rápido - ROADMAP MVP

## 🎯 Soporte de Bases de Datos

Este proyecto ahora soporta tanto **MySQL** como **Oracle** con JDBC. Puedes cambiar entre ellas dinámicamente.

### Parámetros de Conexión

#### MySQL
- Host (ej: localhost)
- Puerto (default: 3306)
- Base de datos (ej: roadmap_mvp)
- Usuario (ej: root)
- Contraseña

#### Oracle
- Host/Servidor (ej: oracle-server.company.com)
- Puerto (default: 1521)
- SID (ej: ORCL)
- Usuario (ej: system)
- Contraseña

---

## 🚀 Instalación y Ejecución

### Backend

```bash
cd roadmap-mvp-project\backend
mvn clean install
```

#### Opción 1: SimpleServer (puerto 8081)
```bash
java -cp target/roadmap-backend-0.0.1-SNAPSHOT.jar com.example.roadmap.SimpleServer
```

#### Opción 2: Spring Boot (puerto 8080)
```bash
mvn spring-boot:run
```

### Frontend

```bash
cd roadmap-mvp-project\frontend
npm install
ng serve
```

O versión alternativa:
```bash
javac FrontendServer.java
java FrontendServer
```

---

## 📍 Acceder a las aplicaciones

| Aplicación | URL | Función |
|---|---|---|
| **Frontend** | http://localhost:4200 | UI para crear/listar roadmaps |
| **Backend SimpleServer** | http://localhost:8081/api/roadmaps | REST API |
| **Backend Spring Boot** | http://localhost:8080/api/roadmaps | REST API |

---

## 🔗 Gestionar Conexión de Base de Datos

### Ver estado actual
```powershell
# Verificar qué BD está conectada
$response = Invoke-WebRequest -Uri http://localhost:8080/api/database/status -Method Get
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

### Conectar a MySQL
```powershell
$params = @{
    host = "localhost"
    port = "3306"
    database = "roadmap_mvp"
    user = "root"
    password = ""
}
$queryString = [System.Web.HttpUtility]::ParseQueryString([String]::Empty)
$params.Keys | % { $queryString.Add($_, $params[$_]) }
$uri = "http://localhost:8080/api/database/connect/mysql?$($queryString.ToString())"
Invoke-WebRequest -Uri $uri -Method Post | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### Conectar a Oracle
```powershell
$params = @{
    host = "oracle-server.company.com"
    port = "1521"
    sid = "ORCL"
    user = "system"
    password = "oracle"
}
$queryString = [System.Web.HttpUtility]::ParseQueryString([String]::Empty)
$params.Keys | % { $queryString.Add($_, $params[$_]) }
$uri = "http://localhost:8080/api/database/connect/oracle?$($queryString.ToString())"
Invoke-WebRequest -Uri $uri -Method Post | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### Ver tipos de BD soportadas
```powershell
Invoke-WebRequest -Uri http://localhost:8080/api/database/supported-types -Method Get | `
  Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### Desconectar
```powershell
Invoke-WebRequest -Uri http://localhost:8080/api/database/disconnect -Method Post
```

---

## 🧪 Pruebas manuales (PowerShell/cmd)

### Crear roadmap:
```powershell
$body = @{ title='Mi Roadmap'; description='Primera versión' } | ConvertTo-Json
Invoke-WebRequest -Uri http://localhost:8081/api/roadmaps -Method Post `
  -Body $body -ContentType 'application/json'
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
| Framework | Spring Boot | 3.1.4 |
| BD - SQL | MySQL / Oracle | 8.0 / 19c+ |
| JDBC Drivers | MySQL Connector / OJDBC | 8.0.33 / 23.3+ |
| Frontend | Angular | 17+ |

---

## 📚 Documentación Detallada

- **[DATABASE_CONFIGURATION.md](./docs/DATABASE_CONFIGURATION.md)** - Guía completa de configuración de BD
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Arquitectura del sistema
- **[README.md](./README.md)** - Información general del proyecto

---

## ⚙️ En el Código

### Usar con Maven y Spring Boot

```java
import com.example.roadmap.MySQLConnection;

// MySQL (default)
MySQLConnection.setConnectionParams("localhost", "3306", "roadmap_mvp", "root", "");
Connection conn = MySQLConnection.connect();

// Oracle
Connection conn = MySQLConnection.connectToOracle(
    "oracle-server", "1521", "ORCL", "system", "oracle"
);
```

### O usando la clase universal DatabaseConnection

```java
import com.example.roadmap.DatabaseConnection;

// Configurar para Oracle
DatabaseConnection.setOracleParams("oracle-server", "1521", "ORCL", "system", "oracle");
DatabaseConnection.setDatabaseType("ORACLE");
Connection conn = DatabaseConnection.connect();

// Alternar a MySQL en tiempo de ejecución
DatabaseConnection.setDatabaseType("MYSQL");
DatabaseConnection.setMySQLParams("localhost", "3306", "roadmap_mvp", "root", "");
conn = DatabaseConnection.connect();
```

---

## 🐛 Solución de Problemas

### MySQL no conecta
- Verifica que MySQL esté corriendo en `localhost:3306`
- Verifica credenciales de usuario/contraseña

### Oracle no conecta
- Verifica que el driver OJDBC esté disponible: `mvn clean install`
- Verifica el SID es correcto
- Verifica las credenciales de acceso

---

## 📝 Próximos Pasos

1. Set up your database (MySQL or Oracle)
2. Configure connection parameters usando la API REST
3. Build and run the backend
4. Launch the frontend
5. Use the API to manage roadmaps
| Servidor Backend | HttpServer (JDK) | - |
| Servidor Frontend | HttpServer (JDK) | - |
| Persistencia | En memoria | MVP |

---

## 💡 Notas

- ✅ **Sin dependencias externas:** Solo JDK 17+ (no requiere Maven, npm, npm, etc.)
- 📦 **Arquitectura hexagonal:** Limpia separación de capas en backend
- 🎨 **Frontend responsivo:** CSS3 moderno sin frameworks
- 📝 **Documentación completa:** requisitos, análisis, diseño en `/docs`
