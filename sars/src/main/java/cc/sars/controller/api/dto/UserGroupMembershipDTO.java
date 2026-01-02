package cc.sars.controller.api.dto;

import cc.sars.model.Role;

public class UserGroupMembershipDTO {
    private String nombreGrupo;
    private Role rolEnGrupo;

    public UserGroupMembershipDTO(String nombreGrupo, Role rolEnGrupo) {
        this.nombreGrupo = nombreGrupo;
        this.rolEnGrupo = rolEnGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public Role getRolEnGrupo() {
        return rolEnGrupo;
    }

    public void setRolEnGrupo(Role rolEnGrupo) {
        this.rolEnGrupo = rolEnGrupo;
    }
}
