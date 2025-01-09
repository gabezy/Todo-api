package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.TokenDTO;
import br.com.gabezy.todoapi.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDTO> authenticate(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authenticationService.authenticate(loginDTO));
    }

}
