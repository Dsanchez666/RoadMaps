# Inicio Rapido - ROADMAP MVP

## Resumen
Este proyecto se ejecuta con Spring Boot (backend en puerto 8080) + Angular (frontend en puerto 4200).

## Requisitos
- Java 17+
- Maven 3.8+
- Node.js 20.19+ (LTS), 21.7.3 (entorno actual) o 22.12+ y npm (si usas Angular 20)

## Backend
### Spring Boot (8080)
```bash
cd roadmap-mvp-project/backend
mvn clean install
mvn spring-boot:run
```

## Frontend
### Angular (4200)
```bash
cd roadmap-mvp-project/frontend
npm install
npm run start
```

## URLs
- Frontend Angular: http://localhost:4200
- Backend Spring Boot: http://localhost:8080

## API de roadmaps
- `POST /api/roadmaps`
- `GET /api/roadmaps`
- `GET /api/roadmaps/{id}`

## API de configuracion de BD
- `POST /api/database/connect/mysql`
- `POST /api/database/connect/oracle`
- `GET /api/database/status`
- `GET /api/database/supported-types`
- `POST /api/database/disconnect`

## Ejemplos PowerShell (Spring Boot)
### Estado
```powershell
$response = Invoke-WebRequest -Uri http://localhost:8080/api/database/status -Method Get
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

### Conectar MySQL
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

### Conectar Oracle
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

## Notas importantes
- En el estado actual, `RoadmapController` (Spring) usa repositorio en memoria.
- La configuracion JDBC de MySQL/Oracle aplica a la capa de conexion (`DatabaseConnection`), no implica automaticamente persistencia de roadmaps en BD relacional.
- Si usas Angular, recuerda mantener `proxy.conf.json` alineado con el backend que tengas levantado.

## Documentacion relacionada
- `README.md`
- `ARCHITECTURE.md`
- `docs/DATABASE_CONFIGURATION.md`
- `docs/ORACLE_INTEGRATION.md`
