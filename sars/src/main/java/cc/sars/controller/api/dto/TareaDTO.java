package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas;

public class TareaDTO {

    private String nombre;
    private EstadosTareas estado;
    private String usuarioAsignado;

    public TareaDTO() {
    }

    public TareaDTO(String nombre, EstadosTareas estado, String usuarioAsignado) {
        this.nombre = nombre;
        this.estado = estado;
        this.usuarioAsignado = usuarioAsignado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadosTareas getEstado() {
        return estado;
    }

    public void setEstado(EstadosTareas estado) {
        this.estado = estado;
    }

    public String getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(String usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }
}
