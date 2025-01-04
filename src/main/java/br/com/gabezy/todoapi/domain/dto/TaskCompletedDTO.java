package br.com.gabezy.todoapi.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "TaskCompleted")
public record TaskCompletedDTO(

        @NotNull
        @Schema(description = "Task's completed status")
        Boolean completed
)
{
}
