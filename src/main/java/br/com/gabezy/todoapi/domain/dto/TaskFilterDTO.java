package br.com.gabezy.todoapi.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaskFilter")
public record TaskFilterDTO(

        @Schema(description = "Task's content")
        String content,

        @Schema(description = "Task's completed status")
        Boolean completed
) {
}
