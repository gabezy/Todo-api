package br.com.gabezy.todoapi.domain.dto;

public record TaskDataDTO(
        Long id,
        String content,
        Boolean completed
) {
}
