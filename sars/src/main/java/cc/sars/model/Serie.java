package cc.sars.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "serie", uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_nombre", "nombre"}))
public class Serie {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty("nombreSerie")
    private String nombre;

    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_nombre") // Apunta a Grupo.nombre
    @JsonIgnore 
    private Grupo grupo;
    
    @JsonProperty("capitulos")
    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "capitulo_indice")
    private List<Capitulo> capitulos = new ArrayList<>();
    
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

    public void removeCapitulo(Capitulo capitulo) {
        this.capitulos.remove(capitulo);
        capitulo.setSerie(null);
    }

    public Integer getId() {
        return id;
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

    public List<Capitulo> getCapitulos() {
        return capitulos;
    }
    
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Serie serie = (Serie) o;
        return Objects.equals(id, serie.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}