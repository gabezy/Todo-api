package br.com.gabezy.todoapi.domain.dto;

import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateUserDTO(
        @Email
        String email,

        @NotBlank
        String password,

        @NotEmpty
        List<RoleName> roles
) {
}
