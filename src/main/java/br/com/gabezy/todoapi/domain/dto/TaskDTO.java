package br.com.gabezy.todoapi.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskDTO(
        @NotBlank
        String content,
        @NotNull
        Boolean completed
) {
}
