package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UserInfoDTO;
import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.UserRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRespository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserDTO createUserDTO;

    private User user;

    private Role userRole;

    private String encodedPassword;

    @BeforeEach
    void setUp() {
        createUserDTO = new CreateUserDTO("test@example.com", "password123");
        encodedPassword = "encodedPassword123";

        userRole = new Role();
        userRole.setName(RoleName.USER);

        user = new User();
        user.setId(1L);
        user.setEmail(createUserDTO.email());
        user.setPassword(encodedPassword);
        user.setRoles(List.of(userRole));
    }

    @Test
    void should_createAndReturnUser() {
        when(passwordEncoder.encode(createUserDTO.password())).thenReturn(encodedPassword);
        when(roleService.findByName(RoleName.USER)).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(createUserDTO);

        assertNotNull(result);
        assertEquals(createUserDTO.email(), result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(1, result.getRoles().size());
        assertEquals(RoleName.USER, result.getRoles().get(0).getName());

        verify(passwordEncoder).encode(createUserDTO.password());
        verify(roleService).findByName(RoleName.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_findAndReturnUserInfoDTO() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserInfoDTO result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getEmail(), result.email());
        assertEquals(1, result.roles().size());
        assertEquals(user.getCreatedAt(), result.createdAt());

        verify(userRepository).findById(anyLong());
    }

    @Test
    void should_throw_resourceNotFoundException_find_user_by_nonExisting_id() {
        Long id = 100L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> userService.findById(id));
    }

    @Test
    void should_find_existingUser_by_email() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void should_throw_resourceNotFoundException_find_user_by_nonExisting_email() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class, () ->
                userService.findByEmail(email)
        );
        verify(userRepository).findByEmail(email);
    }

}