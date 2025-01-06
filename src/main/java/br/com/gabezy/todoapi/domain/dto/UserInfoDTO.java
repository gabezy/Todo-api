package br.com.gabezy.todoapi.domain.dto;

import br.com.gabezy.todoapi.domain.entity.Role;

import java.time.OffsetDateTime;
import java.util.List;

public record UserInfoDTO(
        Long id,
        String email,
        List<Role> roles,
        OffsetDateTime createdAt
) {
}
