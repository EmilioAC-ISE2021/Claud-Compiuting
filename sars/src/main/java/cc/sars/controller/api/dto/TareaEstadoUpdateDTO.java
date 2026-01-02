package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas;

public class TareaEstadoUpdateDTO {
    private EstadosTareas nuevoEstado;
    private String username;

    // Getters y Setters
    public EstadosTareas getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadosTareas nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
