package cc.sars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

@Embeddable 
public class Tarea {

    @Column(name = "nombre_tarea", nullable = false)
    private String nTarea;

    @Column(name = "usuario")
    private String usuario = "NADIE";

    @Enumerated(EnumType.STRING) 
    @Column(name = "estadoTarea")
    private EstadosTareas estado = EstadosTareas.NoAsignado;

    // Constructor vacío
    public Tarea() {
    }

    public Tarea(String nTarea) {
        this.nTarea = nTarea;
    }

    public String setUsuarioAsignado(String u) {
        this.usuario = u;
        return this.usuario;
    }

    public EstadosTareas setEstadoTarea(EstadosTareas e) {
        this.estado = e;
        return this.estado;
    }

    // --- Getters ---
    public String getNombre() {
        return nTarea;
    }
    public String getUsuarioAsignado() {
        return this.usuario;
    }
    public EstadosTareas getEstadoTarea() {
        return this.estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        return Objects.equals(nTarea, tarea.nTarea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nTarea);
    }
}