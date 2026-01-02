package cc.sars.model;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioGrupoId implements Serializable {

    private String usuario;
    private String grupo;

    public UsuarioGrupoId() {}

    public UsuarioGrupoId(String usuario, String grupo) {
        this.usuario = usuario;
        this.grupo = grupo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioGrupoId that = (UsuarioGrupoId) o;
        return Objects.equals(usuario, that.usuario) &&
               Objects.equals(grupo, that.grupo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, grupo);
    }
}
