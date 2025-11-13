package cc.sars.controller.api.dto;

import cc.sars.model.Role;
import java.util.Set;

public class UserDTO {
    private String username;
    private Role role;
    private Set<String> grupos;

    public UserDTO(String username, Role role, Set<String> grupos) {
        this.username = username;
        this.role = role;
        this.grupos = grupos;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<String> getGrupos() {
        return grupos;
    }

    public void setGrupos(Set<String> grupos) {
        this.grupos = grupos;
    }
}
