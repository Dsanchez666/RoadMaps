# Startup Profile (Referencia Operativa)

Este proyecto tiene varias versiones instaladas en la maquina. Algunas son incompatibles entre si:

- Node por defecto: `v21.7.3` (usado en este entorno).
- Java por defecto en PATH: `1.8.0_202` (insuficiente para Spring Boot 3.1.x).
- JDK disponible compatible: `C:\Program Files\Java\jdk-22`.
- Node compatible definido por proyecto: `v21.7.3` (via Node instalado en PATH).

## Regla operativa fija

- Backend siempre con `JAVA_HOME` forzado a `JDK 22`.
- Frontend siempre con Node en PATH (actualmente `v21.7.3`).
- No se usan servidores propios (`SimpleServer` / `FrontendServer`).
- Flujo unico: Spring Boot `:8080` + Angular `:4200` con proxy `/api -> :8080`.

## Fichero de arranque

Usar `startup-profile.ps1` con una accion:

```powershell
cd roadmap-mvp-project
powershell -ExecutionPolicy Bypass -File .\startup-profile.ps1 check
powershell -ExecutionPolicy Bypass -File .\startup-profile.ps1 backend
powershell -ExecutionPolicy Bypass -File .\startup-profile.ps1 backend-test
powershell -ExecutionPolicy Bypass -File .\startup-profile.ps1 frontend
```

## Por que este perfil

- Evita fallos por mezclar Java 8 con Spring Boot 3.
- Evita fallos del CLI Angular al ejecutar con Node 21.
- Evita prompts interactivos de Angular CLI en arranque automatizado (`CI=true`, `NG_CLI_ANALYTICS=false`).
- Estandariza puertos y contrato de proxy para pruebas locales.
