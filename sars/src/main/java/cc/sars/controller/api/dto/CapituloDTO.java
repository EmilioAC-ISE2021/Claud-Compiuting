package cc.sars.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CapituloDTO {

    @JsonProperty("nombreCapitulo")
    private String nombre;

    public CapituloDTO() {
    }

    public CapituloDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
