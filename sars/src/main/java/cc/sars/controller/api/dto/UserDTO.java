package cc.sars.controller.api.dto;

import java.util.List;

public class UserDTO {
    private String username;
    private List<UserGroupMembershipDTO> membresiasGrupo;

    public UserDTO(String username, List<UserGroupMembershipDTO> membresiasGrupo) {
        this.username = username;
        this.membresiasGrupo = membresiasGrupo;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<UserGroupMembershipDTO> getMembresiasGrupo() {
        return membresiasGrupo;
    }

    public void setMembresiasGrupo(List<UserGroupMembershipDTO> membresiasGrupo) {
        this.membresiasGrupo = membresiasGrupo;
    }
}
