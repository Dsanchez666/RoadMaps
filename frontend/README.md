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
