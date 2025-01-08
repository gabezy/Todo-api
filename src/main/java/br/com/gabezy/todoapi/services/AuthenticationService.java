package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.detail.UserDetailsImpl;
import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.TokenDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    public TokenDTO authenticate(LoginDTO dto) {
        var usernameAndPasswordAuth = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());

        Authentication auth = authenticationManager.authenticate(usernameAndPasswordAuth);

        UserDetails userDetails = (UserDetailsImpl) auth.getPrincipal();

        return new TokenDTO(jwtTokenService.generateToken(userDetails));
    }


}
