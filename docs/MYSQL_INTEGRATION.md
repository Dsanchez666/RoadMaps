# Roadmap MVP v0.5 - MySQL Integration

## Descripción de cambios

La versión v0.5 introduce soporte completo para almacenamiento en **MySQL**, manteniendo compatibilidad total con **JSON**.

### Nuevas características

1. **Pantalla de Configuración de BBDD**: Aparece al iniciar la aplicación
   - Opción para conectar a MySQL
   - Opción para usar almacenamiento JSON local
   - Visualización del estado de la conexión

2. **API de base de datos**: Endpoints REST
   - `POST /api/database/connect/mysql` - Conectar a MySQL
   - `POST /api/database/connect/oracle` - Conectar a Oracle
   - `GET /api/database/status` - Verificar estado de conexión
   - `POST /api/database/disconnect` - Desconectar

3. **Datos de conexión personalizables**:
   - Host (default: localhost)
   - Puerto (default: 3306)
   - Base de datos (default: roadmap_mvp)
   - Usuario (default: root)
   - Contraseña (opcional)

## Instalación y Configuración

### Requisitos previos

- Java 17+
- MySQL Server 5.7+ (para usar la opción MySQL)

### Pasos de instalación

#### 1. Instalación de MySQL (si no está instalado)

**Windows:**
```bash
# Descargar desde https://dev.mysql.com/downloads/mysql/
# Ejecutar el instalador
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install mysql-server
sudo mysql_secure_installation
```

**macOS (con Homebrew):**
```bash
brew install mysql
mysql_secure_installation
```

#### 2. Crear la base de datos

```bash
# Conectarse a MySQL como root
mysql -u root -p

# Ejecutar el script SQL
source backend/database/schema.sql;

# Verificar que se creó la base de datos
SHOW DATABASES;
use roadmap_mvp;
SHOW TABLES;
```

O directamente desde terminal:
```bash
mysql -u root -p < backend/database/schema.sql
```

#### 3. Driver JDBC de MySQL

El proyecto ya incluye la dependencia de MySQL en el `pom.xml`. No es necesario descargar el driver manualmente.

#### 4. Iniciar backend y frontend

```bash
# Terminal 1: Backend
cd C:\SideProjects\RoadMaps\backend
mvn spring-boot:run

# Terminal 2: Frontend
cd C:\SideProjects\RoadMaps\frontend
npm run start
```

#### 5. Acceder a la aplicación

Abre tu navegador en: `http://localhost:4200`

## Estructura de la base de datos

### Tabla: roadmaps
```sql
- id (VARCHAR 50, PRIMARY KEY)
- titulo (VARCHAR 255)
- descripcion (LONGTEXT)
- producto (VARCHAR 255)
- organizacion (VARCHAR 255)
- horizonte_inicio (VARCHAR 20)
- horizonte_fin (VARCHAR 20)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Tabla: ejes_estrategicos
```sql
- id (VARCHAR 50, PRIMARY KEY)
- roadmap_id (VARCHAR 50, FOREIGN KEY)
- nombre (VARCHAR 255)
- descripcion (LONGTEXT)
- color (VARCHAR 7)
- posicion (INT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Tabla: iniciativas
```sql
- id (VARCHAR 50, PRIMARY KEY)
- roadmap_id (VARCHAR 50, FOREIGN KEY)
- eje_id (VARCHAR 50, FOREIGN KEY)
- nombre (VARCHAR 255)
- descripcion (LONGTEXT)
- inicio (VARCHAR 20)
- fin (VARCHAR 20)
- certeza (VARCHAR 50)
- dependencias (JSON)
- posicion (INT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## Solución de problemas

### Error: "Failed to connect to database"
- Verifica que MySQL esté corriendo
- Comprueba las credenciales de conexión
- Asegúrate de haber ejecutado el script de schema.sql

### Cambiar de MySQL a JSON o viceversa
- Abre el navegador con las herramientas de desarrollador (F12)
- Ve a Application → Local Storage
- Busca la entrada `dbConfig`
- Puedes editarla o eliminarla para volver a la pantalla de configuración

```javascript
// En la consola JavaScript:
localStorage.removeItem('dbConfig');
location.reload();
```

## Próximas mejoras planeadas

- [ ] Migración automática de JSON a MySQL
- [ ] Panel de administración de BBDD
- [ ] Backups automáticos
- [ ] Sincronización en tiempo real
- [ ] Soporte multi-usuario
- [ ] PostgreSQL como alternativa

---

**Versión:** v0.5
**Fecha:** 2026-02-13
**Autor:** Development Team
