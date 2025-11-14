# Endpoints de la API

## AuthController.java
*   `GET /login`
*   `GET /register`
*   `POST /register`

## AdminController.java
*   `GET /admin`
*   `POST /admin/grupo/eliminar`
*   `POST /admin/usuario/eliminar`

## IndexController.java
*   `GET /`

## SerieController.java
*   `POST /serie/crear`
*   `GET /serie/{nombreSerie}`
*   `POST /serie/{nombreSerie}/capitulo/crear`
*   `POST /serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/crear`
*   `POST /serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/estado`
*   `POST /serie/{nombreSerie}/capitulo/{nombreCapitulo}/tarea/{nombreTarea}/asignarUsuario`
*   `POST /serie/{nombreSerie}/capitulo/{nombreCapitulo}/eliminar`
*   `POST /serie/{nombreSerie}/eliminar`

## GrupoController.java
*   `GET /grupo/gestionar`
*   `POST /grupo/gestionar/agregar`
*   `POST /grupo/gestionar/cambiar-rol`
*   `POST /grupo/gestionar/eliminarUsuario`
*   `POST /grupo/eliminar`

## GrupoRestController.java
*   `GET /api/grupos`
*   `GET /api/grupos/{nombre}`
*   `POST /api/grupos`
*   `DELETE /api/grupos/{nombre}`

## CapituloRestController.java
*   `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos`
*   `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}`
*   `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos`
*   `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/bulk`
*   `DELETE /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}`

## UsuarioGrupoRestController.java
*   `POST /api/usuarios/{username}/grupos/{nombreGrupo}`
*   `DELETE /api/usuarios/{username}/grupos/{nombreGrupo}`
*   `PUT /api/usuarios/{username}/grupos/{nombreGrupo}/rol`

## UserRestController.java
*   `GET /api/usuarios`
*   `GET /api/usuarios/{username}`
*   `POST /api/usuarios`
*   `PUT /api/usuarios/{username}`
*   `DELETE /api/usuarios/{username}`

## TareaRestController.java
*   `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas`
*   `POST /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas`
*   `PUT /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`
*   `GET /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`
*   `DELETE /api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}/tareas/{nombreTarea}`
