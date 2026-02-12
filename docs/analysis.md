# Análisis - Roadmap MVP

## Contexto
Se dispone de un MVP (configurador de roadmap). Este proyecto es una reimplementación enfocada en una API backend y un cliente Angular.

## Decisiones clave
- Java 17 + Spring Boot para backend por madurez y productividad.
- Arquitectura hexagonal para desacoplar lógica de negocio de frameworks/externalidades.
- Repositorio en memoria para MVP (fácil testing). Sustituible por JPA u otra implementación.

## Casos de uso principales
- CrearRoadmap: validar entrada mínima (título obligatorio), crear entidad y persistir.
- ListRoadmaps: devolver todos los roadmaps.
- GetRoadmap: devolver por id o 404.
