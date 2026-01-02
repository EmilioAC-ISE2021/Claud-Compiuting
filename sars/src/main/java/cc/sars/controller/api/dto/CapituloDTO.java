package cc.sars.controller.api.dto;

import java.util.List;

public class CapituloDTO {
    private String nombreCapitulo;
    private String nombreSerie; // Add serie name for context
    private List<TareaDTO> tareas;

    public CapituloDTO(String nombreCapitulo, String nombreSerie, List<TareaDTO> tareas) {
        this.nombreCapitulo = nombreCapitulo;
        this.nombreSerie = nombreSerie;
        this.tareas = tareas;
    }

    // Getters
    public String getNombreCapitulo() {
        return nombreCapitulo;
    }

    public String getNombreSerie() {
        return nombreSerie;
    }

    public List<TareaDTO> getTareas() {
        return tareas;
    }
}