package br.com.gabezy.todoapi.domain.dto;

public record CreateUserDTO(
        String email,
        String password
) {
}
