package cc.sars.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "usuario_grupo")
@IdClass(UsuarioGrupoId.class)
public class UsuarioGrupo implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "username")
    private User usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "grupo_nombre")
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role rol;

    public UsuarioGrupo() {
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }
}
