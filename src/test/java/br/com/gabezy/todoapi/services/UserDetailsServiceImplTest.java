package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest{

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private static User user;

    @BeforeAll
    static void setUpAll() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.ADMINISTRATOR);

        user = new User();
        user.setId(1L);
        user.setEmail("jonhDoe@email.com");
        user.setPassword("secret");
        user.setRoles(List.of(role));
    }

    @Test
    void should_load_user_by_username() {
        when(userService.findByEmail(user.getEmail()))
                .thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("jonhDoe@email.com");

        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> {
                    String roleName = user.getRoles().get(0).getName().name();
                    return grantedAuthority.getAuthority().equals(roleName);
                })
        );
    }

}