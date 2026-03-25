Roadmap Frontend (Angular)

Esta carpeta contiene la app Angular del proyecto.

Pasos:

1. Instalar dependencias

```bash
cd roadmap-mvp-project/frontend
npm install
```

2. Ejecutar en desarrollo (proxy /api -> backend)

```bash
npm run start
```

3. Validar build y tests

```bash
npm run build
npm run test:ci
```

Notas:
- Usa Node.js 20.19+ LTS o 22.12+ para Angular 20.
- El proxy de `proxy.conf.json` apunta a Spring Boot en `http://localhost:8080`.
- Rutas Angular principales:
  - `/database`: conexión a BBDD.
  - `/roadmaps`: listado, búsqueda y acceso a ver/editar.
  - `/roadmaps/:id/view`: visualización de roadmap.
  - `/roadmaps/:id/edit`: edición de configuración del roadmap.
- Flujo JSON:
  - Al seleccionar archivo JSON se muestra una vista previa temporal.
  - `Guardar` persiste en backend/MySQL.
  - `Descartar` limpia la carga temporal y refresca el listado desde backend.
- Seguridad:
  - Las credenciales de conexión no se persisten en `localStorage/sessionStorage`.
  - La opción de `Reconectar` reutiliza credenciales en memoria solo durante la sesión actual de la pestaña.
