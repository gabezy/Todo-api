package br.com.gabezy.todoapi.domain.dto;

import jakarta.validation.constraints.NotNull;

public record TaskCompletedDTO(
        @NotNull
        Boolean completed
)
{
}
