package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UpdateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UserDTO;
import br.com.gabezy.todoapi.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "Operations relate to Users")
public class UserController implements GenericCrudController<UserDTO, Long, CreateUserDTO, UpdateUserDTO> {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<Void> create(CreateUserDTO createDTO, UriComponentsBuilder builder) {
        var user = userService.createUser(createDTO);
        URI uri = builder.path("users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Override
    public ResponseEntity<UserDTO> findById(Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Override
    public ResponseEntity<Void> update(Long id, UpdateUserDTO updateDTO) {
        userService.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
