package br.com.gabezy.todoapi.controllers.generics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface GenericFilteredController <F, T> {

    @GetMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get resource by filter", description = "Get list of resources based on filter",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieve all task")
    public ResponseEntity<List<T>> findByFilter(F filter);

}
