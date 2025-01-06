package br.com.gabezy.todoapi.domain.dto;

import br.com.gabezy.todoapi.domain.enumaration.RoleName;

public record UserFilterDTO(
        String email,
        RoleName roleName
) {
}
