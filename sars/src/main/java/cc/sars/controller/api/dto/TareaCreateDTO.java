package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas; // Import the enum

public class TareaCreateDTO {

    private String nombre;
    private EstadosTareas estadoTarea; // Add estadoTarea
    private String usuarioAsignado; // Add usuarioAsignado

    public TareaCreateDTO() {
    }

    public TareaCreateDTO(String nombre) {
        this.nombre = nombre;
    }

    // New constructor to include all fields
    public TareaCreateDTO(String nombre, EstadosTareas estadoTarea, String usuarioAsignado) {
        this.nombre = nombre;
        this.estadoTarea = estadoTarea;
        this.usuarioAsignado = usuarioAsignado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Getters and Setters for new fields
    public EstadosTareas getEstadoTarea() {
        return estadoTarea;
    }

    public void setEstadoTarea(EstadosTareas estadoTarea) {
        this.estadoTarea = estadoTarea;
    }

    public String getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(String usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }
}
