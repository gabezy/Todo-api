package br.com.gabezy.todoapi.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Task")
public record TaskDTO(

        @NotBlank
        @Schema(description = "Task's content")
        String content,

        @NotNull
        @Schema(description = "Task's completed status")
        Boolean completed
) {
}
