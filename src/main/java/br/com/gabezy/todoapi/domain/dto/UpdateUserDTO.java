package br.com.gabezy.todoapi.domain.dto;

import br.com.gabezy.todoapi.domain.entity.Role;

import java.util.List;

public record UpdateUserDTO(
        String email,
        String password,
        List<Role> roles
) {
}
