# Frontend Angular - guia rapida

Esta carpeta YA contiene la app Angular del proyecto.

## Requisitos
- Node.js 20.19+ LTS o 22.12+
- npm 8+

## Arranque
```bash
cd roadmap-mvp-project/frontend
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
- `src/app/services`: servicios HTTP (`roadmap.service.ts`, `database.service.ts`).
- `src/app/components`: UI de configuracion BD, alta y listado de roadmaps.
- `proxy.conf.json`: redireccion de `/api` al backend activo.
