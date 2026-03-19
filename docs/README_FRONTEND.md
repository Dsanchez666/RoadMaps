# Frontend Angular - guia rapida

Esta carpeta YA contiene la app Angular del proyecto.

## Requisitos
- Node.js 20.19+ LTS o 22.12+
- npm 8+

## Arranque
```bash
cd C:\SideProjects\RoadMaps\frontend
npm install
ng serve --proxy-config proxy.conf.json
```

## Build y tests
```bash
npm run build
npm run test:ci
```

## Seguridad
```bash
npm audit
npm audit --omit=dev
```

Nota: `npm audit` puede reportar vulnerabilidades de herramientas de desarrollo. Revisar siempre `npm audit --omit=dev` para riesgo de runtime.

## Estructura relevante
- `src/app/features/roadmaps/services`: servicios HTTP de roadmaps.
- `src/app/features/database/services`: servicios HTTP de base de datos.
- `src/app/features/roadmaps/components`: UI de alta y listado.
- `src/app/features/database/components`: UI de configuracion BD.
- `src/app/shared/models`: modelos compartidos.
- `proxy.conf.json`: redireccion de `/api` al backend activo.
