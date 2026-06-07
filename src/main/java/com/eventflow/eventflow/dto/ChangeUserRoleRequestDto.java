package com.eventflow.eventflow.dto;

import com.eventflow.eventflow.model.Role;
import jakarta.validation.constraints.NotNull;

public class ChangeUserRoleRequestDto {

    @NotNull
    private Role role;

    public ChangeUserRoleRequestDto() {
    }

    public ChangeUserRoleRequestDto(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
