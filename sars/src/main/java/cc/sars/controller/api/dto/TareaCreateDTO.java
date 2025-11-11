package cc.sars.controller.api.dto;

public class TareaCreateDTO {

    private String nombre;

    public TareaCreateDTO() {
    }

    public TareaCreateDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
