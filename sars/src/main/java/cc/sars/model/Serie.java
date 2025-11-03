package cc.sars.model;

import java.util.List;         
import java.util.ArrayList;    
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.OrderColumn; 

@Entity
@Table(name = "serie")
public class Serie {

    @Id
    @JsonProperty("nombreSerie")
    private String nombre;

    private String descripcion;

    @JsonProperty("capitulos")
    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "capitulo_indice")
    private List<Capitulo> capitulos = new ArrayList<>(); // Ahora es una List

    // --- Constructor vacío ---
    public Serie() {
    }

    public Serie(String n, String d) {
        this.nombre = n;
        this.descripcion = d;
    }

    public Serie(String n, String d, List<Capitulo> c) {
        this.nombre = n;
        this.descripcion = d;
        c.forEach(this::addCapitulo); 
    }

    public void addCapitulo(Capitulo capitulo) {
        if (!this.capitulos.contains(capitulo)) {
            this.capitulos.add(capitulo);
            capitulo.setSerie(this);
        }
    }

    // (removeCapitulo funciona igual con List)
    public void removeCapitulo(Capitulo capitulo) {
        this.capitulos.remove(capitulo);
        capitulo.setSerie(null);
    }

    // --- (Getters y Setters sin cambios, excepto el tipo de 'getCapitulos') ---
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

    // --- CAMBIO 6: El getter devuelve List ---
    public List<Capitulo> getCapitulos() {
        return capitulos;
    }

    // --- equals y hashCode (sin cambios) ---
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