package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.detail.UserDetailsImpl;
import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = JwtTokenService.class)
@ExtendWith(SpringExtension.class)
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    private static final User user = new User();

    @BeforeAll
    static void setUpAll() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.ADMINISTRATOR);

        user.setId(1L);
        user.setEmail("jonhDoe@email.com");
        user.setPassword("secret");
        user.setRoles(List.of(role));
    }

    @Test
    void should_generate_token() {
        UserDetails userDetails = new UserDetailsImpl(user);

        String token = jwtTokenService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void should_get_subject_from_token() {
        UserDetails userDetails = new UserDetailsImpl(user);

        String token = jwtTokenService.generateToken(userDetails);

        assertEquals(user.getEmail(), jwtTokenService.getSubjectFromToken(token));
    }

}