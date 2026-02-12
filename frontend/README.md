Roadmap Frontend

Este esqueleto Angular es una implementación mínima para consumir la API backend.

Pasos para ejecutar:

1. Instalar dependencias:

```bash
cd roadmap-mvp-project/frontend
npm install
```

2. Ejecutar (usa proxy para redirigir `/api` al backend):

```bash
ng serve --proxy-config proxy.conf.json
```

Notas:
- Si no tienes Angular CLI global, instala `npm i -g @angular/cli` o usa `npx ng`.
- El backend debe estar corriendo en `http://localhost:8080`.
