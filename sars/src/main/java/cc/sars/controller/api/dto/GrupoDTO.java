package cc.sars.controller.api.dto;

public class GrupoDTO {
    private String nombre;

    public GrupoDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
