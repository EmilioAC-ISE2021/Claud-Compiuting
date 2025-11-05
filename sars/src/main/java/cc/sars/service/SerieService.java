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

    public SerieService(SerieRepository serieRepository, CapituloRepository capituloRepository, GrupoRepository grupoRepository) {
        this.serieRepository = serieRepository;
        this.capituloRepository = capituloRepository;
        this.grupoRepository = grupoRepository;
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
        
        // Llamada al método 'addCapitulo' de la entidad Serie
        serie.addCapitulo(nuevoCapitulo);

        return serieRepository.save(serie);
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

        // LÓGICA DE BLOQUEO: Si la tarea ya está asignada a OTRA persona
        if (!usuarioAsignado.equals("NADIE") && !usuarioAsignado.equals(nombreUsuarioActual)) {
            throw new RuntimeException("La tarea '" + nombreTarea + "' ya está asignada a " + usuarioAsignado + ".");
        }

        // LÓGICA DE ASIGNACIÓN/LIBERACIÓN:
        
        // 1. Si el nuevo estado es "Asignado", se auto-asigna al usuario actual.
        if (nuevoEstado == EstadosTareas.Asignado) {
            tareaAActualizar.setUsuarioAsignado(nombreUsuarioActual);
        } 
        // 2. Si el nuevo estado es "NoAsignado", se libera (solo el dueño puede hacerlo).
        else if (nuevoEstado == EstadosTareas.NoAsignado) {
            tareaAActualizar.setUsuarioAsignado("NADIE");
        }

        tareaAActualizar.setEstadoTarea(nuevoEstado);
        return capituloRepository.save(capitulo);
    }

    /**
     * Devuelve todos los valores posibles del Enum EstadosTareas.
     */
    @Transactional(readOnly = true)
    public List<EstadosTareas> getTodosLosEstados() {
        return Arrays.asList(EstadosTareas.values());
    }
}