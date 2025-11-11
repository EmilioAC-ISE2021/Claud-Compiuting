package cc.sars.controller.api.dto;

import cc.sars.model.EstadosTareas;

public class TareaUpdateDTO {

    private EstadosTareas estado;
    private String usuarioAsignado;

    public TareaUpdateDTO() {
    }

    public TareaUpdateDTO(EstadosTareas estado, String usuarioAsignado) {
        this.estado = estado;
        this.usuarioAsignado = usuarioAsignado;
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
