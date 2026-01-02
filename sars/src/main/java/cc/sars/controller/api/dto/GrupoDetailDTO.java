package cc.sars.controller.api.dto;

import java.util.List;

public class GrupoDetailDTO {
    private String nombre;
    private List<String> series;
    private List<GrupoMemberDTO> miembros;

    public GrupoDetailDTO(String nombre, List<String> series, List<GrupoMemberDTO> miembros) {
        this.nombre = nombre;
        this.series = series;
        this.miembros = miembros;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getSeries() {
        return series;
    }

    public void setSeries(List<String> series) {
        this.series = series;
    }

    public List<GrupoMemberDTO> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<GrupoMemberDTO> miembros) {
        this.miembros = miembros;
    }
}
