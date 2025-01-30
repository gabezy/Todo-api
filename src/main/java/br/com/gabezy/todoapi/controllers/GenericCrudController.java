package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.config.expectionhandler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpHeaders.LOCATION;

public interface GenericCrudController<T, ID, C, U> {


    @Operation(summary = "Create Resource", description = "Create resource based in the request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resource created successfully",
                    headers = @Header(name = LOCATION, description = "Resource URI")),
            @ApiResponse(responseCode = "400", description = "Invalid fields in the request body",
                    content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> create(@Valid @RequestBody C createDTO, UriComponentsBuilder builder);

    @ApiResponse(responseCode = "200", description = "Successfully retrieve all resources")
    @Operation(summary = "Get all resources", description = "Get list of all resources in page")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Page<T>> findAll(Pageable pageable);

    @Operation(summary = "Get resource by ID", description = "Get resource based on ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Resource not found (ID doesn't exist)",
                    content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> findById(@PathVariable("id") ID id);

    @Operation(summary = "Update resource", description = "Update the entire resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid fields in the request body" ,
                    content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "404", description = "Task not found (ID doesn't exist)",
                    content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> update(@PathVariable("id") ID id, @Valid @RequestBody U updateDTO);

    @Operation(summary = "Delete resource by ID", description = "Delete resource based on the ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Resource not found (ID doesn't exist)",
                    content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> delete(@PathVariable("id") ID id);

}
