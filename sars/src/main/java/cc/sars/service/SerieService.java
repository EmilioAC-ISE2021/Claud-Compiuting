package cc.sars.service;

import cc.sars.model.Capitulo;
import cc.sars.model.EstadosTareas;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.repository.CapituloRepository;
import cc.sars.repository.SerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Capa de servicio para gestionar la lógica de negocio de Series,
 * Capítulos y Tareas.
 * @Transactional asegura que todos los métodos que modifican la BD
 * se ejecuten dentro de una transacción.
 */
@Service
@Transactional
public class SerieService {

    private final SerieRepository serieRepository;
    private final CapituloRepository capituloRepository;

    // Inyección de dependencias (los repositorios)
    public SerieService(SerieRepository serieRepository, CapituloRepository capituloRepository) {
        this.serieRepository = serieRepository;
        this.capituloRepository = capituloRepository;
    }

    // --- MÉTODOS PARA SERIES ---

    /**
     * Obtiene todas las series de la base de datos.
     * (Para la página 'index')
     */
    @Transactional(readOnly = true) // readOnly = true optimiza las consultas de solo lectura
    public List<Serie> getAllSeries() {
        return serieRepository.findAll();
    }

    /**
     * Busca una serie específica por su nombre (ID).
     * (Para la página 'administrar serie')
     */
    @Transactional(readOnly = true)
    public Optional<Serie> getSerieByNombre(String nombre) {
        return serieRepository.findByNombre(nombre);
    }

    /**
     * Crea y guarda una nueva serie.
     * (Para el formulario de 'index')
     */
    public Serie createSerie(String nombre, String descripcion) {
        if (serieRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Error: La serie con el nombre '" + nombre + "' ya existe.");
        }
        Serie nuevaSerie = new Serie(nombre, descripcion);
        return serieRepository.save(nuevaSerie);
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
     * (Para el formulario de 'añadir capítulo')
     */
    public Serie addCapituloToSerie(String nombreSerie, String nombreCapitulo) {
        // 1. Encontrar la serie
        Serie serie = getSerieByNombre(nombreSerie)
                .orElseThrow(() -> new RuntimeException("No se encontró la serie: " + nombreSerie));

        // 2. Comprobar si el capítulo ya existe (opcional, pero buena idea)
        if (capituloRepository.findByNombre(nombreCapitulo).isPresent()) {
             throw new RuntimeException("Error: El capítulo con el nombre '" + nombreCapitulo + "' ya existe.");
        }

        // 3. Crear el nuevo capítulo
        Capitulo nuevoCapitulo = new Capitulo(nombreCapitulo);

        // 4. Añadirlo a la serie (el método 'addCapitulo' de Serie se encarga de la relación)
        serie.addCapitulo(nuevoCapitulo);

        // 5. Guardar la serie. Gracias a CascadeType.ALL, el capítulo también se guardará.
        return serieRepository.save(serie);
    }

    // --- MÉTODOS PARA TAREAS ---

    /**
     * Crea una nueva tarea (Embeddable) y la añade a un capítulo existente.
     * (Para el formulario de 'añadir tarea')
     */
    public Capitulo addTareaToCapitulo(String nombreCapitulo, String nombreTarea) {
        // 1. Encontrar el capítulo
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        // 2. Crear la nueva tarea
        Tarea nuevaTarea = new Tarea(nombreTarea);

        // 3. Añadir la tarea al capítulo (el Set<Tarea> en Capitulo evita duplicados)
        capitulo.anyadirTarea(nuevaTarea);

        // 4. Guardar el capítulo (con su nueva colección de tareas)
        return capituloRepository.save(capitulo);
    }

    /**
     * Actualiza el estado de una tarea específica dentro de un capítulo.
     * (Para el desplegable de 'estado')
     */
    public Capitulo updateTareaEstado(String nombreCapitulo, String nombreTarea, EstadosTareas nuevoEstado) {
        // 1. Encontrar el capítulo
        Capitulo capitulo = getCapituloByNombre(nombreCapitulo)
                .orElseThrow(() -> new RuntimeException("No se encontró el capítulo: " + nombreCapitulo));

        // 2. Encontrar la tarea específica dentro del Set
        Tarea tareaAActualizar = capitulo.getTareas().stream()
                .filter(tarea -> tarea.getNombre().equals(nombreTarea))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la tarea: " + nombreTarea));

        // 3. Actualizar el estado
        tareaAActualizar.setEstadoTarea(nuevoEstado);

        // 4. Guardar la entidad padre (Capitulo)
        return capituloRepository.save(capitulo);
    }

    /**
     * Devuelve todos los valores posibles del Enum EstadosTareas.
     * (Para poblar los desplegables en el frontend)
     */
    @Transactional(readOnly = true)
    public List<EstadosTareas> getTodosLosEstados() {
        return Arrays.asList(EstadosTareas.values());
    }
}