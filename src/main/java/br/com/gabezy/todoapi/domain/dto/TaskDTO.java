package br.com.gabezy.todoapi.domain.dto;

public record TaskDTO(
        String content,
        Boolean completed
) {
}
