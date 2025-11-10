package cc.sars.service;

import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Grupo;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.model.User; // Importar User
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.GrupoRepository;
import cc.sars.repository.SerieRepository;
import cc.sars.repository.UserRepository; // Added import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SerieService {

    private final SerieRepository serieRepository;
    private final CapituloRepository capituloRepository;
    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository; // Añadir dependencia

    public SerieService(SerieRepository serieRepository, CapituloRepository capituloRepository, GrupoRepository grupoRepository, UserRepository userRepository) { // Modificar constructor
        this.serieRepository = serieRepository;
        this.capituloRepository = capituloRepository;
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
    }

    // --- MÉTODOS PARA SERIES ---

    /**
     * Obtiene todas las series de un grupo específico.
     */
    @Transactional(readOnly = true)
    public List<Serie> getSeriesPorGrupo(String nombreGrupo) {
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
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
     * Crea una nueva serie y la asigna a un grupo.
     */
    public Serie createSerie(String nombre, String descripcion, String nombreGrupo) {
        if (serieRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Error: La serie con el nombre '" + nombre + "' ya existe.");
        }
        
        Grupo grupo = grupoRepository.findByNombre(nombreGrupo)
                 .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Serie nuevaSerie = new Serie(nombre, descripcion);
        
        // Llamada al método 'agregarSerie' de la entidad Grupo
        grupo.agregarSerie(nuevaSerie); 
        grupoRepository.save(grupo);
        return nuevaSerie;
    }

    public void deleteSerie(String nombreSerie) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new RuntimeException("No se encontró la serie: " + nombreSerie));
        serieRepository.delete(serie);
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
                .orElseThrow(() -> new RuntimeException("No se encontró la serie: " + nombreSerie));

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
                .orElseThrow(() -> new RuntimeException("No se encontró la serie: " + nombreSerie));

        // Dividir la cadena de nombres de capítulos por saltos de línea y procesar cada uno
        Arrays.stream(nombresCapitulos.split("\\r?\\n"))
              .map(String::trim)
              .filter(nombre -> !nombre.isEmpty())
              .forEach(nombreCapitulo -> {
                  if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
                      System.err.println("Advertencia: El capítulo con el nombre '" + nombreCapitulo + "' ya existe y será omitido.");
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
                                    System.err.println("Advertencia: Formato de tarea en masa incorrecto: " + tareaData);
                                }
                            });
                  }
                  serie.addCapitulo(nuevoCapitulo);
              });

        return serieRepository.save(serie);
    }

    public void deleteCapitulo(String nombreSerie, String nombreCapitulo) {
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new RuntimeException("No se encontró la serie: " + nombreSerie));

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
     * Actualiza el estado de una tarea.
     * Gestiona la asignación (al usuario actual) y el bloqueo de la tarea.
     */
        public Capitulo updateTareaEstado(String nombreCapitulo, String nombreTarea, EstadosTareas nuevoEstado, User usuarioActual) {
    
            Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                    .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));
    
            Tarea tareaAActualizar = capitulo.getTareas().stream()
                    .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró la tarea: " + nombreTarea));
    
            String usuarioAsignado = tareaAActualizar.getUsuarioAsignado();
            String nombreUsuarioActual = usuarioActual.getUsername();
            EstadosTareas estadoActual = tareaAActualizar.getEstadoTarea(); // Obtener estado actual
    
            // Lógica para LÍDER: Puede cambiar el estado como quiera
            if (usuarioActual.getRole() == cc.sars.model.Role.ROLE_LIDER) {
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
                    throw new RuntimeException("No puedes cambiar el estado de las tareas en este capítulo porque la tarea 'CC' está completada.");
                }
                
                // Condición especial para ROLE_QC: puede marcar como "Repetir" una tarea "Completado" de otro.
                if (usuarioActual.getRole() == cc.sars.model.Role.ROLE_QC &&
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
    public void asignarUsuarioATarea(String nombreSerie, String nombreCapitulo, String nombreTarea, String nuevoUsuarioAsignadoUsername, User lider) {
        // 1. Autorización: Solo un LÍDER puede asignar usuarios
        if (lider.getRole() != cc.sars.model.Role.ROLE_LIDER) {
            throw new RuntimeException("Solo un LÍDER puede asignar usuarios a las tareas.");
        }

        // 2. Encontrar el capítulo
        Capitulo capitulo = capituloRepository.findByNombre(nombreCapitulo)
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        // 3. Encontrar la tarea
        Tarea tareaAActualizar = capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la tarea: " + nombreTarea));

        // 4. Verificar que el nuevo usuario asignado existe
        User nuevoUsuario = userRepository.findByUsername(nuevoUsuarioAsignadoUsername)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario a asignar: " + nuevoUsuarioAsignadoUsername));

        // 5. Actualizar el usuario asignado
        tareaAActualizar.setUsuarioAsignado(nuevoUsuario.getUsername());

        // 6. Guardar el capítulo (las tareas se guardan en cascada)
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
