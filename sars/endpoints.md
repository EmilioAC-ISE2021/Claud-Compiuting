# API Endpoints

## Grupos

- `GET /api/grupos`
- `GET /api/grupos/{nombre}`
- `POST /api/grupos`
- `DELETE /api/grupos/{nombre}`

## Series

- `GET /api/grupos/{nombreGrupo}/series`
- `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}`
- `POST /api/grupos/{nombreGrupo}/series`
- `PUT /api/grupos/{nombreGrupo}/series/{nombreSerie}`
- `DELETE /api/grupos/{nombreGrupo}/series/{nombreSerie}`

## Capítulos

- `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos`
- `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}`
- `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos`
- `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/bulk`
- `DELETE /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}`

## Tareas

- `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas`
- `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`
- `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas`
- `PUT /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`
- `DELETE /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`

## Usuarios

- `GET /api/usuarios`
- `GET /api/usuarios/{username}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{username}`
- `DELETE /api/usuarios/{username}`

## Relación Usuario-Grupo

- `POST /api/usuarios/{username}/grupos/{nombreGrupo}`
- `DELETE /api/usuarios/{username}/grupos/{nombreGrupo}`
- `PUT /api/usuarios/{username}/grupos/{nombreGrupo}/rol`

---

# Resumen de Tests de la API Restful

- **Grupos**: Se comprueba el ciclo de vida completo: listar todos los grupos, obtener uno por nombre, crearlo (manejando conflictos si ya existe), y eliminarlo.
- **Series**: Se asegura que se puedan listar, obtener, crear, actualizar y eliminar series dentro del contexto de un grupo.
- **Capítulos**: Se valida el listado, obtención, creación (individual y en masa), y eliminación de capítulos para una serie.
- **Tareas**: Se verifica el listado, obtención, creación, actualización (de estado y asignación) y eliminación de tareas para un capítulo.
- **Usuarios**: Se comprueba la gestión de usuarios, incluyendo listado, obtención por nombre, creación, actualización y eliminación.
- **Asociación Usuario-Grupo**: Se prueba que se pueda añadir y eliminar un usuario de un grupo, y cambiar su rol dentro de ese grupo.
