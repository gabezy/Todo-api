package br.com.gabezy.todoapi.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @Email
        String email,

        @NotBlank
        String password
) {
}
