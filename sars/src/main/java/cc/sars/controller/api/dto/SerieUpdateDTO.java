package cc.sars.controller.api.dto;

public class SerieUpdateDTO {

    private String descripcion;

    public SerieUpdateDTO() {
    }

    public SerieUpdateDTO(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
