package br.com.gabezy.todoapi.domain.dto;

public record TaskFilterDTO(
        String content,
        Boolean completed
) {
}
