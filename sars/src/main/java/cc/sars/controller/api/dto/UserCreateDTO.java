package cc.sars.controller.api.dto;

import cc.sars.model.Role;

public class UserCreateDTO {
    private String username;
    private String password;
    private Role rolEnGrupo;
    private String nombreGrupo;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRolEnGrupo() {
        return rolEnGrupo;
    }

    public void setRolEnGrupo(Role rolEnGrupo) {
        this.rolEnGrupo = rolEnGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }
}
