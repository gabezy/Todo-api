package br.com.gabezy.todoapi.config.expectionhandler;

import java.util.Map;

public record ResponseError(
        String code,
        String description,
        Map<String, String> fields
) {
}
