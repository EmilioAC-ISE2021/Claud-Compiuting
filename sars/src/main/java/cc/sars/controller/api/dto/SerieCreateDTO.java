package cc.sars.controller.api.dto;

public class SerieCreateDTO {

    private String nombre;
    private String descripcion;
    private String nombreGrupo;

    public SerieCreateDTO() {
    }

    public SerieCreateDTO(String nombre, String descripcion, String nombreGrupo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nombreGrupo = nombreGrupo;
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

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }
}
