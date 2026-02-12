# Release Notes - Roadmap MVP

## v0.3 - Edit Roadmap Feature
**Fecha:** 2026-02-12

### Nuevas Características
- **Edición de Roadmaps**: Botón EDITAR en listado y en visualización de roadmaps
  - Interfaz visual completa para editar datos base, ejes e iniciativas
  - Tablas con CRUD (crear, leer, actualizar, eliminar) para ejes e iniciativas
  - Modales para edición de ejes y iniciativas con todos los campos

- **Guardado Flexible**: Al guardar cambios, se pregunta:
  - Sobreescribir el roadmap actual
  - Guardar como nuevo roadmap (crea una copia)
  - Opción de descartar cambios

- **Interfaz de Edición**:
  - Datos base: Título, producto, organización, horizonte
  - Tabla de ejes con ID, nombre, descripción y color
  - Tabla de iniciativas con nombre, eje, inicio, fin
  - Botones para añadir, editar y eliminar

### Mejoras
- Interfaz responsive con dos columnas en pantallas grandes
- Confirmación antes de descartar cambios
- Navegación clara entre vistas
- Persistencia automática en localStorage

### Cambios Técnicos
- Nueva vista: `editView`
- Nuevos modales: `ejeModalOverlay`, `iniciativaModalOverlay`, `saveModalOverlay`
- Funciones de edición: `editCurrentRoadmap()`, `editRoadmapFromList()`, `loadEditForm()`
- CRUD de ejes: `addNewEje()`, `editEje()`, `saveEjeModal()`, `removeEje()`
- CRUD de iniciativas: `addNewIniciativa()`, `editIniciativa()`, `saveIniciativaModal()`, `removeIniciativa()`
- Guardado: `saveEditedRoadmap()`, `saveOverwriteRoadmap()`, `saveAsNewRoadmap()`
- Estilos para tablas, modales y botones de edición

---

## v0.2 - Home Screen & Roadmap Management
**Fecha:** 2026-02-12

### Nuevas Características
- **Pantalla de Inicio Mejorada**: Nueva interfaz principal con tres opciones principales:
  - ➕ Crear nuevo roadmap desde cero
  - 📥 Importar desde JSON
  - 📋 Ver roadmaps existentes

- **Creación de Roadmaps**: Formulario para crear nuevos roadmaps sin necesidad de JSON
  - Título, producto, organización
  - Horizonte base (inicio y fin)
  - IDs generados automáticamente con timestamp

- **Listado de Roadmaps**: Vista de roadmaps existentes con:
  - Nombre, producto, organización
  - Contador de ejes e iniciativas
  - Botones para ver y eliminar

- **Persistencia Mejorada**: Almacenamiento en localStorage además del servidor
  - Los roadmaps se guardan localmente y funcionan sin conexión
  - Sincronización con servidor cuando está disponible

### Mejoras
- Interfaz reorganizada con navegación clara
- Cards visuales para seleccionar acciones
- Contador de datos en el listado
- Estado vacío informativo
- Mejor gestión de vistas (homeView, importView, listView, createView, roadmapView)

### Cambios Técnicos
- Nuevas funciones: `showView()`, `goToHome()`, `showImportView()`, `showListView()`, `showCreateRoadmapView()`
- Funciones CRUD: `createNewRoadmap()`, `loadAndDisplayRoadmaps()`, `viewRoadmap()`, `deleteRoadmap()`
- Almacenamiento con localStorage
- Generación de IDs con timestamp
- Estilos CSS para cards y listados

---

## v0.1 - Initiative Modal & Dependency Visualization
**Fecha:** 2026-02-12

### Nuevas Características
- **Modal de Iniciativas**: Al hacer clic en una iniciativa en el roadmap, se abre un modal con todos los detalles:
  - ID, nombre, objetivo
  - Tipo, expediente, periodo (inicio-fin)
  - Impacto principal, usuarios afectados
  - Certeza (comprometido/planificado/exploratorio)
  - Listado de dependencias

- **Visualización de Dependencias**: Al pasar el ratón sobre una iniciativa:
  - Se oscurecen todas las demás iniciativas
  - Se resaltan en rojo claro las iniciativas que dependen de ella
  - Al salir, vuelve el estado normal

### Mejoras
- UI mejorada con mejor interactividad
- Gestión de eventos de ratón para mostrar dependencias
- Cierre de modal con tecla Escape

### Cambios Técnicos
- Añadidos estilos CSS para modal y efectos de dependencias
- Funciones JavaScript: `showInitiativaModal()`, `highlightDependencies()`, `clearDependencyHighlight()`
- Atributos data en filas de iniciativas para referenciación
- Métodos auxiliares: `findInitiativeById()`, `attachInitiativeHoverListeners()`

---

## v0.0 - Initial Release
**Fecha:** 2026-02-11

### Características Iniciales
- **Importación de Roadmaps desde JSON**: Carga un archivo JSON con la estructura completa del roadmap
- **Visualización de Timeline**: Visor de roadmap con timeline de 20 trimestres (5 años)
- **Ejes Estratégicos**: Agrupación visual de iniciativas por ejes con colores distintos
- **Iniciativas**: Visualización de barras de tiempo con indicador de certeza
- **Persistencia**: Almacenamiento en servidor mediante SimpleServer y archivos JSON
- **Descarga de Roadmap**: Opción para descargar el roadmap en formato JSON

### Componentes Principales
- **Backend**: SimpleServer.java con API REST para CRUD de roadmaps
- **Frontend**: HTML/JavaScript con interfaz para importar y visualizar roadmaps
- **Datos**: Estructura JSON con producto, organización, ejes_estratégicos, iniciativas

### Estructura de Datos
```json
{
  "producto": "string",
  "organizacion": "string",
  "horizonte_base": { "inicio": "YYYY-TQ", "fin": "YYYY-TQ" },
  "ejes_estrategicos": [
    { "id": "string", "nombre": "string", "descripcion": "string", "color": "#hex" }
  ],
  "iniciativas": [
    {
      "id": "string",
      "nombre": "string",
      "eje": "string",
      "tipo": "string",
      "expediente": "string",
      "inicio": "YYYY-TQ",
      "fin": "YYYY-TQ",
      "objetivo": "string",
      "impacto_principal": "string",
      "usuarios_afectados": "string",
      "dependencias": [{ "iniciativa": "string", "tipo": "string" }],
      "certeza": "comprometido|planificado|exploratorio"
    }
  ]
}
```

