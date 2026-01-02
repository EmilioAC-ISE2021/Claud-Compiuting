package cc.sars.service;

import cc.sars.exception.SerieNotFoundException;
import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Grupo;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.model.User;
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.SerieRepository;
import cc.sars.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cc.sars.exception.CcTaskCompletedException;
import cc.sars.exception.AssignmentForbiddenException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class SerieService {

    private static final Logger logger = LoggerFactory.getLogger(SerieService.class);

    private final SerieRepository serieRepository;
    private final CapituloRepository capituloRepository;
    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;
    private final @Lazy UsuarioService usuarioService;

    public SerieService(SerieRepository serieRepository, CapituloRepository capituloRepository, GrupoRepository grupoRepository, UserRepository userRepository, @Lazy UsuarioService usuarioService) {
        this.serieRepository = serieRepository;
        this.capituloRepository = capituloRepository;
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
        this.usuarioService = usuarioService;
    }

    // --- MÉTODOS PARA SERIES ---

    /**
     * Obtiene todas las series de un grupo específico.
     */
    @Transactional(readOnly = true)
    public List<Serie> getSeriesPorGrupo(String nombreGrupo) {
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new SerieNotFoundException("Grupo no encontrado"));
        return grupo.getSeries();
    }

    /**
     * Busca una serie específica por su nombre (ID).
     */
    @Transactional(readOnly = true)
    public Optional<Serie> getSerieByNombre(String nombre) {
        return serieRepository.findByNombre(nombre);
    }

    /**
     * Obtiene todas las series.
     */
    @Transactional(readOnly = true)
    public List<Serie> buscarTodas() {
        return serieRepository.findAll();
    }

    /**
     * Crea una nueva serie y la asigna a un grupo.
     */
    public Serie createSerie(String nombre, String descripcion, String nombreGrupo) {
        if (serieRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Error: La serie con el nombre '" + nombre + "' ya existe.");
        }
        
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                 .orElseThrow(() -> new SerieNotFoundException("Grupo no encontrado"));

        Serie nuevaSerie = new Serie(nombre, descripcion);
        
        // Llamada al método 'agregarSerie' de la entidad Grupo
        grupo.agregarSerie(nuevaSerie); 
        grupoRepository.save(grupo);
        return nuevaSerie;
    }

    public void deleteSerie(String nombreSerie) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie));
        serieRepository.delete(serie);
    }

    public Serie updateSerie(String nombre, String descripcion) {
        Serie serie = getSerieByNombre(nombre)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombre));
        serie.setDescripcion(descripcion);
        return serieRepository.save(serie);
    }

    /**
     * Busca una serie específica por su nombre (ID) dentro de un grupo.
     */
    @Transactional(readOnly = true)
    public Optional<Serie> getSerieByNombreAndGrupo(String nombreGrupo, String nombreSerie) {
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new SerieNotFoundException("Grupo no encontrado: " + nombreGrupo));
        return grupo.getSeries().stream()
                .filter(serie -> serie.getNombre().equals(nombreSerie))
                .findFirst();
    }

    public void deleteSerieInGrupo(String nombreGrupo, String nombreSerie) {
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new SerieNotFoundException("Grupo no encontrado: " + nombreGrupo));
        Serie serie = grupo.getSeries().stream()
                .filter(s -> s.getNombre().equals(nombreSerie))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie + " en el grupo: " + nombreGrupo));
        
        grupo.getSeries().remove(serie);
        serieRepository.delete(serie);
        grupoRepository.save(grupo);
    }

    public Serie updateSerieInGrupo(String nombreGrupo, String nombreSerie, String descripcion) {
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new SerieNotFoundException("Grupo no encontrado: " + nombreGrupo));
        Serie serie = grupo.getSeries().stream()
                .filter(s -> s.getNombre().equals(nombreSerie))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie + " en el grupo: " + nombreGrupo));
        
        serie.setDescripcion(descripcion);
        return serieRepository.save(serie);
    }

    // --- MÉTODOS PARA CAPÍTULOS ---

    /**
     * Busca un capítulo específico por su nombre (ID).
     */
    @Transactional(readOnly = true)
    public Optional<Capitulo> getCapituloByNombre(String nombre) {
        return capituloRepository.findByNombre(nombre);
    }

    /**
     * Crea un nuevo capítulo y lo añade a una serie existente.
     */
    public Serie addCapituloToSerie(String nombreSerie, String nombreCapitulo) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie));

        if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
             throw new RuntimeException("Error: El capítulo con el nombre '" + nombreCapitulo + "' ya existe.");
        }

        Capitulo nuevoCapitulo = new Capitulo(nombreCapitulo);
        
        // Añadir la tarea 'CC' por defecto
        Tarea tareaCC = new Tarea("CC");
        nuevoCapitulo.anyadirTarea(tareaCC);
        
        // Llamada al método 'addCapitulo' de la entidad Serie
        serie.addCapitulo(nuevoCapitulo);

        return serieRepository.save(serie);
    }

    /**
     * Crea múltiples capítulos a partir de una cadena de nombres separados por saltos de línea
     * y les asigna tareas en masa.
     */
    public Serie addCapitulosToSerie(String nombreSerie, String nombresCapitulos, String[] tareasEnMasa) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie));

        // Dividir la cadena de nombres de capítulos por saltos de línea y procesar cada uno
        Arrays.stream(nombresCapitulos.split("\r?\n"))
              .map(String::trim)
              .filter(nombre -> !nombre.isEmpty())
              .forEach(nombreCapitulo -> {
                  if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
                      logger.warn("El capítulo con el nombre '{}' ya existe y será omitido.", nombreCapitulo);
                      return; // Saltar este capítulo y continuar con el siguiente
                  }

                  Capitulo nuevoCapitulo = new Capitulo(nombreCapitulo);

                  // Añadir las tareas en masa si existen
                  if (tareasEnMasa != null) {
                      Arrays.stream(tareasEnMasa)
                            .map(String::trim)
                            .filter(tareaData -> !tareaData.isEmpty())
                            .forEach(tareaData -> {
                                String[] parts = tareaData.split("###");
                                if (parts.length == 3) {
                                    String nombreTarea = parts[0];
                                    EstadosTareas estadoTarea = EstadosTareas.valueOf(parts[1]);
                                    String usuarioAsignado = parts[2];

                                    Tarea nuevaTarea = new Tarea(nombreTarea);
                                    nuevaTarea.setEstadoTarea(estadoTarea);
                                    nuevaTarea.setUsuarioAsignado(usuarioAsignado);
                                    nuevoCapitulo.anyadirTarea(nuevaTarea);
                                } else {
                                    logger.warn("Formato de tarea en masa incorrecto: {}", tareaData);
                                }
                            });
                  }
                  serie.addCapitulo(nuevoCapitulo);
              });

        return serieRepository.save(serie);
    }

    public void deleteCapitulo(String nombreSerie, String nombreCapitulo) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie));

        Capitulo capitulo = serie.getCapitulos().stream()
                .filter(c -> c.getNombre().equals(nombreCapitulo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        serie.removeCapitulo(capitulo);
        capituloRepository.delete(capitulo);
        serieRepository.save(serie);
    }

    /**
     * Crea un nuevo capítulo y lo añade a una serie existente, validando el grupo.
     */
    public Serie addCapituloToSerie(String nombreGrupo, String nombreSerie, String nombreCapitulo) {
        Serie serie = getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie + " en el grupo: " + nombreGrupo));

        if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
             throw new RuntimeException("Error: El capítulo con el nombre '" + nombreCapitulo + "' ya existe.");
        }

        Capitulo nuevoCapitulo = new Capitulo(nombreCapitulo);
        
        // Añadir la tarea 'CC' por defecto
        Tarea tareaCC = new Tarea("CC");
        nuevoCapitulo.anyadirTarea(tareaCC);
        
        // Llamada al método 'addCapitulo' de la entidad Serie
        serie.addCapitulo(nuevoCapitulo);

        return serieRepository.save(serie);
    }

    /**
     * Crea múltiples capítulos y los añade a una serie existente, validando el grupo.
     */
    public Serie addCapitulosToSerie(String nombreGrupo, String nombreSerie, String nombresCapitulos, String[] tareasEnMasa) {
        Serie serie = getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie + " en el grupo: " + nombreGrupo));

        // Dividir la cadena de nombres de capítulos por saltos de línea y procesar cada uno
        Arrays.stream(nombresCapitulos.split("\r?\n"))
              .map(String::trim)
              .filter(nombre -> !nombre.isEmpty())
              .forEach(nombreCapitulo -> {
                  if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
                      logger.warn("El capítulo con el nombre '{}' ya existe y será omitido.", nombreCapitulo);
                      return; // Saltar este capítulo y continuar con el siguiente
                  }

                  Capitulo nuevoCapitulo = new Capitulo(nombreCapitulo);

                  // Añadir las tareas en masa si existen
                  if (tareasEnMasa != null) {
                      Arrays.stream(tareasEnMasa)
                            .map(String::trim)
                            .filter(tareaData -> !tareaData.isEmpty())
                            .forEach(tareaData -> {
                                String[] parts = tareaData.split("###");
                                if (parts.length == 3) {
                                    String nombreTarea = parts[0];
                                    EstadosTareas estadoTarea = EstadosTareas.valueOf(parts[1]);
                                    String usuarioAsignado = parts[2];

                                    Tarea nuevaTarea = new Tarea(nombreTarea);
                                    nuevaTarea.setEstadoTarea(estadoTarea);
                                    nuevaTarea.setUsuarioAsignado(usuarioAsignado);
                                    nuevoCapitulo.anyadirTarea(nuevaTarea);
                                } else {
                                    logger.warn("Formato de tarea en masa incorrecto: {}", tareaData);
                                }
                            });
                  }
                  serie.addCapitulo(nuevoCapitulo);
              });

        return serieRepository.save(serie);
    }

    /**
     * Elimina un capítulo de una serie, validando el grupo.
     */
    public void deleteCapitulo(String nombreGrupo, String nombreSerie, String nombreCapitulo) {
        Serie serie = getSerieByNombreAndGrupo(nombreGrupo, nombreSerie)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la serie: " + nombreSerie + " en el grupo: " + nombreGrupo));

        Capitulo capitulo = serie.getCapitulos().stream()
                .filter(c -> c.getNombre().equals(nombreCapitulo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        serie.removeCapitulo(capitulo);
        capituloRepository.delete(capitulo);
        serieRepository.save(serie);
    }


    /**
     * Desasigna a un usuario de todas las tareas que tiene asignadas.
     */
    public void desasignarUsuarioDeTareas(String username) {
        List<Capitulo> allCapitulos = capituloRepository.findAll();

        for (Capitulo capitulo : allCapitulos) {
            boolean capituloModificado = false;
            for (Tarea tarea : capitulo.getTareas()) {
                if (username.equals(tarea.getUsuarioAsignado()) && tarea.getEstadoTarea() == EstadosTareas.Asignado) {
                    tarea.setUsuarioAsignado("NADIE");
                    tarea.setEstadoTarea(EstadosTareas.NoAsignado);
                    capituloModificado = true;
                }
            }
            if (capituloModificado) {
                capituloRepository.save(capitulo);
            }
        }
    }

    // --- MÉTODOS PARA TAREAS ---

    /**
     * Busca una tarea específica por su nombre dentro de un capítulo.
     */
    @Transactional(readOnly = true)
    public Optional<Tarea> getTareaByNombre(String nombreCapitulo, String nombreTarea) {
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró el capítulo: " + nombreCapitulo));

        return capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst();
    }

    /**
     * Crea una nueva tarea y la añade a un capítulo existente.
     */
    public Capitulo addTareaToCapitulo(String nombreCapitulo, String nombreTarea) {
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        Tarea nuevaTarea = new Tarea(nombreTarea);

        // --- CORRECCIÓN ---
        // Llamada al método 'anyadirTarea' de la entidad Capitulo
        capitulo.anyadirTarea(nuevaTarea);

        return capituloRepository.save(capitulo);
    }

    /**
     * Elimina una tarea de un capítulo.
     */
    public void deleteTarea(String nombreCapitulo, String nombreTarea) {
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró el capítulo: " + nombreCapitulo));

        Tarea tareaAEliminar = capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la tarea: " + nombreTarea + " en el capítulo: " + nombreCapitulo));

        capitulo.quitarTarea(tareaAEliminar);
        capituloRepository.save(capitulo);
    }


    public Tarea updateTarea(String nombreCapitulo, String nombreTarea, EstadosTareas nuevoEstado, String nuevoUsuarioAsignado) {
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró el capítulo: " + nombreCapitulo));

        Tarea tareaAActualizar = capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst()
                .orElseThrow(() -> new SerieNotFoundException("No se encontró la tarea: " + nombreTarea));

        tareaAActualizar.setEstadoTarea(nuevoEstado);
        tareaAActualizar.setUsuarioAsignado(nuevoUsuarioAsignado);
        capituloRepository.save(capitulo);
        return tareaAActualizar;
    }

    /**
     * Actualiza el estado de una tarea.
     * Gestiona la asignación (al usuario actual) y el bloqueo de la tarea.
     */
        public Capitulo updateTareaEstado(String nombreCapitulo, String nombreTarea, EstadosTareas nuevoEstado, String nombreUsuarioActual) {
            Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                    .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));
            
            // Obtener el grupo asociado a la tarea
            Serie serie = capitulo.getSerie();
            Grupo grupo = serie.getGrupo();

            Tarea tareaAActualizar = capitulo.getTareas().stream()
                    .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró la tarea: " + nombreTarea));
    
            String usuarioAsignado = tareaAActualizar.getUsuarioAsignado();
            EstadosTareas estadoActual = tareaAActualizar.getEstadoTarea(); // Obtener estado actual
    
            User usuarioActual = usuarioService.findByUsername(nombreUsuarioActual)
                    .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado: " + nombreUsuarioActual));
    
            // Lógica para LÍDER: Puede cambiar el estado como quiera
            if (usuarioService.esLiderEnGrupo(usuarioActual, grupo)) {
                if (nuevoEstado == EstadosTareas.Asignado) {
                    tareaAActualizar.setUsuarioAsignado(nombreUsuarioActual);
                } else if (nuevoEstado == EstadosTareas.NoAsignado || nuevoEstado == EstadosTareas.Repetir) {
                    tareaAActualizar.setUsuarioAsignado("NADIE");
                } else if (nuevoEstado == EstadosTareas.Completado) {
                    // Cuando un líder completa una tarea, debe ser asignada al líder que la completó.
                    tareaAActualizar.setUsuarioAsignado(nombreUsuarioActual);
                }
            }
            // Lógica para USUARIO y USUARIO_CC: Restricciones
            else {
                // Comprobar si la tarea CC de este capítulo está completada
                Optional<Tarea> ccTaskOptional = capitulo.getTareas().stream()
                        .filter(t -> t.getNombre().equals("CC"))
                        .findFirst();


                if (ccTaskOptional.isPresent() && ccTaskOptional.get().getEstadoTarea() == EstadosTareas.Completado) {
                    // Si la tarea CC está completada, solo el LIDER puede cambiar el estado de la tarea.
                    // Como estamos en el bloque 'else' (no LIDER), lanzar excepción.
                    throw new CcTaskCompletedException("No puedes cambiar el estado de las tareas en este capítulo porque la tarea 'CC' está completada.");
                }
                
                // Condición especial para ROLE_QC: puede marcar como "Repetir" una tarea "Completado" de otro.
                if (usuarioService.esQcEnGrupo(usuarioActual, grupo) &&
                    nuevoEstado == EstadosTareas.Repetir &&
                    estadoActual == EstadosTareas.Completado) {
                    
                    tareaAActualizar.setUsuarioAsignado("NADIE"); // Se libera al repetir
                }
                // Condición principal para ambos: Solo puede cambiar si la tarea está asignada a NADIE o a sí mismo
                else if (!usuarioAsignado.equals("NADIE") && !usuarioAsignado.equals(nombreUsuarioActual)) {
                    throw new RuntimeException("No puedes cambiar el estado de una tarea asignada a " + usuarioAsignado + ".");
                }
    
                // Transiciones permitidas para usuarios
                else if (estadoActual == EstadosTareas.NoAsignado || estadoActual == EstadosTareas.Repetir) {
                    if (nuevoEstado == EstadosTareas.Asignado) {
                        tareaAActualizar.setUsuarioAsignado(nombreUsuarioActual); // Se auto-asigna
                    } else if (nuevoEstado == EstadosTareas.Completado) { // Permitir la finalización directa de tareas no asignadas/repetidas
                        tareaAActualizar.setUsuarioAsignado(nombreUsuarioActual); // Asignar al usuario actual
                    }
                    else {
                        throw new RuntimeException("Solo puedes asignarte esta tarea.");
                    }
                } else if (estadoActual == EstadosTareas.Asignado) {
                    if (nuevoEstado == EstadosTareas.Completado) {
                        // El usuario asignado sigue siendo el mismo al completar
                    } else if (nuevoEstado == EstadosTareas.NoAsignado) { // Permitir desasignar
                        tareaAActualizar.setUsuarioAsignado("NADIE"); // Se libera al desasignar
                    } else {
                        throw new RuntimeException("Solo puedes marcar como completada o desasignar esta tarea.");
                    }
                } else if (estadoActual == EstadosTareas.Completado) {
                    if (nuevoEstado == EstadosTareas.Repetir) {
                        tareaAActualizar.setUsuarioAsignado("NADIE"); // Se libera al repetir
                    } else {
                        throw new RuntimeException("Solo puedes marcar como repetir esta tarea.");
                    }
                } else {
                    throw new RuntimeException("Transición de estado no permitida para usuarios.");
                }
            }
    
            tareaAActualizar.setEstadoTarea(nuevoEstado);
            return capituloRepository.save(capitulo);
        }
    
    /**
     * Asigna un usuario a una tarea específica. Solo un LÍDER puede realizar esta acción.
     */
    public void asignarUsuarioATarea(String nombreSerie, String nombreCapitulo, String nombreTarea, String nuevoUsuarioAsignadoUsername, String liderUsername) {
        // 1. Encontrar el capítulo, la serie y el grupo
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));
        
        Serie serie = capitulo.getSerie(); 
        Grupo grupo = serie.getGrupo();
        
        // 2. Obtener el usuario líder que realiza la acción
        User lider = userRepository.findByUsername(liderUsername)
                .orElseThrow(() -> new RuntimeException("Usuario líder no encontrado: " + liderUsername));

        // 3. Autorización: Verificar que el usuario 'lider' es LÍDER en este grupo
        if (!usuarioService.esLiderEnGrupo(lider, grupo)) {
            throw new AssignmentForbiddenException("Solo un LÍDER del grupo '" + grupo.getNombre() + "' puede asignar usuarios a las tareas.");
        }

        // 4. Encontrar la tarea
        Tarea tareaAActualizar = capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la tarea: " + nombreTarea));

        // 5. Verificar que el nuevo usuario asignado existe
        User nuevoUsuario = userRepository.findByUsername(nuevoUsuarioAsignadoUsername)
                .orElseThrow(() -> new SerieNotFoundException("No se encontró el usuario a asignar: " + nuevoUsuarioAsignadoUsername));

        // 6. Verificar que el nuevo usuario asignado pertenece al grupo de la tarea
        if (!usuarioService.perteneceAGrupo(nuevoUsuario, grupo)) {
            throw new RuntimeException("El usuario a asignar '" + nuevoUsuarioAsignadoUsername + "' no pertenece al grupo '" + grupo.getNombre() + "'.");
        }

        // 7. Actualizar el usuario asignado
        tareaAActualizar.setUsuarioAsignado(nuevoUsuario.getUsername());

        // 8. Guardar el capítulo (las tareas se guardan en cascada)
        capituloRepository.save(capitulo);
    }

    /**
     * Devuelve todos los valores posibles del Enum EstadosTareas.
     */
    @Transactional(readOnly = true)
    public List<EstadosTareas> getTodosLosEstados() {
        return Arrays.asList(EstadosTareas.values());
    }
}
