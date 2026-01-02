package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas;

public class TareaDTO {

    private String nombre;
    private EstadosTareas estadoTarea;
    private String usuarioAsignado;

    public TareaDTO() {
    }

    public TareaDTO(String nombre, EstadosTareas estadoTarea, String usuarioAsignado) {
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
