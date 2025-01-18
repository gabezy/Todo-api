package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDataDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Task", description = "Operation relate to task")
public class TaskController implements GenericCrudController<TaskDataDTO, Long, TaskDTO, TaskDTO> {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public ResponseEntity<Void> create(TaskDTO createDTO, UriComponentsBuilder builder) {
        Long taskId = taskService.createTask(createDTO).getId();
        URI uri = builder.path("tasks/{id}")
                .buildAndExpand(taskId)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<List<TaskDataDTO>> findAll() {
        return ResponseEntity.ok(taskService.findAll());
    }

    @Override
    public ResponseEntity<TaskDataDTO> findById(Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @GetMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get tasks by filter", description = "Get list of tasks based on filter")
    @ApiResponse(responseCode = "200", description = "Successfully retrieve all task")
    public ResponseEntity<List<TaskDataDTO>> findByFilter(TaskFilterDTO filter) {
        return ResponseEntity.ok(taskService.findByFilter(filter));
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change task's completed status", description = "Patch the completed information by the task's ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task successfully patched"),
            @ApiResponse(responseCode = "400", description = "Invalid fields in the request body"),
            @ApiResponse(responseCode = "404", description = "Task not found (ID doesn't exist)")
    })
    public ResponseEntity<Void> patchCompletedTask(@PathVariable("id") Long id, @Valid @RequestBody TaskCompletedDTO request) {
        taskService.patchCompletedStatus(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> update(Long id, TaskDTO updateDTO) {
        taskService.updateTask(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
