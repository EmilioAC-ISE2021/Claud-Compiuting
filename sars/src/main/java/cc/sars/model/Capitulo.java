package cc.sars.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "capitulo")
public class Capitulo {

    @Id
    @JsonProperty("nombreCapitulo")
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "serie_nombre")
    private Serie serie;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "capitulo_tareas", 
        joinColumns = @JoinColumn(name = "capitulo_nombre")
    )
    private Set<Tarea> tareas = new LinkedHashSet<>();

    // Constructor vacío
    public Capitulo() {
    }

    public Capitulo(String n) {
        this.nombre = n;
    }

    // --- MÉTODO SOLICITADO ---

    public void anyadirTarea(Tarea t) {
        this.tareas.add(t); // El Set se encarga de no duplicar
    }

    // --- Otros métodos ---
    public void quitarTarea(Tarea t) {
        this.tareas.remove(t);
    }
    
    public Set<Tarea> getTareas() {
        return tareas;
    }
    public String getNombre() {
        return nombre;
    }
    public void setSerie(Serie serie) {
        this.serie = serie;
    }
    public Serie getSerie() {
        return serie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capitulo capitulo = (Capitulo) o;
        return Objects.equals(nombre, capitulo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}