package cc.sars.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "serie")
public class Serie {

    @Id
    @JsonProperty("nombreSerie")
    private String nombre;

    private String descripcion;

    @JsonProperty("capitulos")
    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Capitulo> capitulos = new LinkedHashSet<>();

    // Constructor vacío
    public Serie() {
    }

    public Serie(String n, String d) {
        this.nombre = n;
        this.descripcion = d;
    }

    // --- MÉTODO SOLICITADO ---

    /**
     * Añade un capítulo a la serie.
     * Importante: También establece la referencia 'serie' dentro del capítulo
     * para mantener la consistencia de la relación bidireccional.
     */
    public void addCapitulo(Capitulo capitulo) {
        this.capitulos.add(capitulo); // El Set evita duplicados
        capitulo.setSerie(this);
    }

    // --- Otros métodos ---
    public void removeCapitulo(Capitulo capitulo) {
        this.capitulos.remove(capitulo);
        capitulo.setSerie(null);
    }

    public String getNombre() {
        return nombre;
    }
    public String setDescripcion(String d) {
        this.descripcion = d;
        return this.descripcion;
    }
    public String getDescripcion() {
        return this.descripcion;
    }
    public Set<Capitulo> getCapitulos() {
        return capitulos;
    }

    // --- equals y hashCode (basados en el nombre) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Serie serie = (Serie) o;
        return Objects.equals(nombre, serie.nombre);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}