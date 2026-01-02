package cc.sars.controller.api.dto;

import cc.sars.model.Role;

public class GrupoMemberDTO {
    private String username;
    private Role rolEnGrupo;

    public GrupoMemberDTO(String username, Role rolEnGrupo) {
        this.username = username;
        this.rolEnGrupo = rolEnGrupo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRolEnGrupo() {
        return rolEnGrupo;
    }

    public void setRolEnGrupo(Role rolEnGrupo) {
        this.rolEnGrupo = rolEnGrupo;
    }
}
