package br.com.gabezy.todoapi.utils;

public class EndpointUtil {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api-docs/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-ui/index.html",
            "/swagger-ui/", "/api-docs", "/v3/api-docs"
    };

    public static final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth", "/users",
    };

    private EndpointUtil() { throw new IllegalStateException("Utility class");}
}
