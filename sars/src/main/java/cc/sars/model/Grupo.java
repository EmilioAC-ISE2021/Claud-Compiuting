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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "grupo_usuarios",
            joinColumns = @JoinColumn(name = "grupo_nombre"),
            inverseJoinColumns = @JoinColumn(name = "user_username")
    )
    private Set<User> usuarios = new HashSet<>();

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
    public Set<User> getUsuarios() { // Devuelve Set<User>
        return usuarios;
    }
    public void setUsuarios(Set<User> usuarios) {
        this.usuarios = usuarios;
    }
    public void agregarUsuario(User usuario) { // Acepta User
        this.usuarios.add(usuario);
    }
    public void removeUsuario(User usuario) {
        this.usuarios.remove(usuario);
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