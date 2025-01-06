package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.detail.UserDetailsImpl;
import br.com.gabezy.todoapi.domain.dto.LoginUserDTO;
import br.com.gabezy.todoapi.domain.dto.TokenDTO;
import br.com.gabezy.todoapi.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginUserDTO loginUserDTO;

    private User user;

    @BeforeEach
    void setUp() {
        loginUserDTO = new LoginUserDTO("test@example.com", "secret");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRoles(Collections.emptyList());
    }

    @Test
    void should_login_successfully_withValidCredentials() {
        Authentication authentication = mock(Authentication.class);
        String token = "test.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserDetailsImpl(user));
        when(jwtTokenService.generateToken(any(UserDetails.class)))
                .thenReturn(token);

        TokenDTO tokenDTO = authenticationService.authenticate(loginUserDTO);

        assertNotNull(tokenDTO);
        assertEquals(token, tokenDTO.token());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

}