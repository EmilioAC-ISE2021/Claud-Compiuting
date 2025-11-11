package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas;

public class TareaResponseDTO {
    private String nombre;
    private EstadosTareas estadoTarea;
    private String usuarioAsignado;

    public TareaResponseDTO(String nombre, EstadosTareas estadoTarea, String usuarioAsignado) {
        this.nombre = nombre;
        this.estadoTarea = estadoTarea;
        this.usuarioAsignado = usuarioAsignado;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public EstadosTareas getEstadoTarea() {
        return estadoTarea;
    }

    public String getUsuarioAsignado() {
        return usuarioAsignado;
    }
}
