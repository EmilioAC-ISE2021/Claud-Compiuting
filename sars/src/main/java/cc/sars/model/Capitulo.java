package cc.sars.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "capitulo")
public class Capitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty("nombreCapitulo")
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "serie_id")
    private Serie serie;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "capitulo_tareas",
        joinColumns = @JoinColumn(name = "capitulo_id")
    )
    @OrderColumn(name = "tarea_indice")
    private List<Tarea> tareas = new ArrayList<>(); // Ahora es una List


    public Capitulo() {
    }

    public Capitulo(String n) {
        this.nombre = n;
    }

    public void anyadirTarea(Tarea t) {
        if (!this.tareas.contains(t)) {
            this.tareas.add(t);
        }
    }

    public void quitarTarea(Tarea t) {
        this.tareas.remove(t);
    }

    public List<Tarea> getTareas() {
        return tareas;
    }

    public Integer getId() {
        return id;
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
        return id != null && Objects.equals(id, capitulo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
