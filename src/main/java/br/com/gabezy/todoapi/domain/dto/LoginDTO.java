package br.com.gabezy.todoapi.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
