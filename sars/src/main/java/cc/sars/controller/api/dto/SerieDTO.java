package cc.sars.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SerieDTO {

    @JsonProperty("nombreSerie")
    private String nombre;
    private String descripcion;

    public SerieDTO() {
    }

    public SerieDTO(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
