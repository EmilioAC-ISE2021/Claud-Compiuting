package cc.sars.controller.api.dto;

import cc.sars.model.Role;

public class UserRoleUpdateDTO {
    private Role rolEnGrupo;

    public Role getRolEnGrupo() {
        return rolEnGrupo;
    }

    public void setRolEnGrupo(Role rolEnGrupo) {
        this.rolEnGrupo = rolEnGrupo;
    }
}
