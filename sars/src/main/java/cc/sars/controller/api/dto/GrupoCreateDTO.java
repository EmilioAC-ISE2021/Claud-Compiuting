package cc.sars.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrupoCreateDTO {

    @JsonProperty("nombreGrupo")
    private String nombre;

    public GrupoCreateDTO() {
    }

    public GrupoCreateDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
