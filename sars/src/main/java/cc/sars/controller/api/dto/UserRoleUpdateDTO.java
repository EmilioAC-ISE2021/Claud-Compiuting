package cc.sars.controller.api.dto;

import cc.sars.model.Role;

public class UserRoleUpdateDTO {
    private Role rol;

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }
}
