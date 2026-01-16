package cc.sars.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "grupo")
public class Grupo {

    @Id
    @Column(unique = true, nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Serie> series = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioGrupo> usuarioGrupos = new HashSet<>();

    // --- Constructores ---
    public Grupo() {
    }
    public Grupo(String nombre) {
        this.nombre = nombre;
    }

    // --- Métodos ---
    public String getNombre() {
        return nombre;
    }
    public Set<UsuarioGrupo> getUsuarioGrupos() {
        return usuarioGrupos;
    }

    public void setUsuarioGrupos(Set<UsuarioGrupo> usuarioGrupos) {
        this.usuarioGrupos = usuarioGrupos;
    }

    public Set<User> getUsuarios() {
        return this.usuarioGrupos.stream()
                .map(UsuarioGrupo::getUsuario)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public List<Serie> getSeries() {
        return series;
    }
    public void agregarSerie(Serie serie) {
        this.series.add(serie);
        serie.setGrupo(this);
    }

    // --- equals y hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grupo grupo = (Grupo) o;
        return Objects.equals(nombre, grupo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}