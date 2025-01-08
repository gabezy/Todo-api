package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UpdateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UserFilterDTO;
import br.com.gabezy.todoapi.domain.dto.UserDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

    private Role userRole;
    private Role adminRole;
    private User user;
    private User admin;
    private User adminAndUser;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        encodedPassword = "encodedPassword123";

        userRole = new Role();
        userRole.setName(RoleName.USER);

        user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setPassword(encodedPassword);
        user.setRoles(List.of(userRole));

        adminRole = new Role();
        adminRole.setName(RoleName.ADMINISTRATOR);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("jane.doe@example.com");
        admin.setPassword(encodedPassword);
        admin.setRoles(List.of(adminRole));

        adminAndUser = new User();
        adminAndUser.setId(3L);
        adminAndUser.setEmail("joshua.maven@example.com");
        adminAndUser.setPassword(encodedPassword);
        adminAndUser.setRoles(List.of(userRole, adminRole));
    }

    @Test
    void should_createAndReturnUser() {
        var createUserDTO = new CreateUserDTO("john.doe@example.com", "password123");

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

        UserDTO result = userService.findById(1L);

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
    void should_findAndReturnListUsersInfoDTO() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDTO> result = userService.findAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).id());
        assertEquals(user.getRoles().size(), result.get(0).roles().size());

        verify(userRepository).findAll();
    }

    @Test
    void should_findAndReturnAllUsers_whenEamilAndRoleNameAreNull() {
        UserFilterDTO filter = new UserFilterDTO(null, null);

        when(userRepository.findByEmailContainingAndRoleName(filter.email(), filter.roleName()))
                .thenReturn(List.of(user, admin, adminAndUser));

        List<UserDTO> result = userService.findByFilter(filter);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(dto ->
                adminAndUser.getEmail().equals(dto.email())));
        assertTrue(result.stream().anyMatch(dto ->
                admin.getEmail().equals(dto.email())));
        assertTrue(result.stream().anyMatch(dto ->
                user.getEmail().equals(dto.email())));

        verify(userRepository).findByEmailContainingAndRoleName(filter.email(), filter.roleName());
    }

    @Test
    void should_findAndReturnListUsersInfoDTO_byPartialEmailFilter() {
        UserFilterDTO filter = new UserFilterDTO("doe", null);

        when(userRepository.findByEmailContainingAndRoleName(filter.email(), filter.roleName()))
                .thenReturn(List.of(user, admin));

        List<UserDTO> result = userService.findByFilter(filter);

        assertEquals(2, result.size(), "Should return two users");
        assertTrue(result.stream().anyMatch(dto ->
                "john.doe@example.com".equals(dto.email())), "Should contain john.doe@example.com");
        assertTrue(result.stream().anyMatch(dto ->
                "jane.doe@example.com".equals(dto.email())), "Should contain jane.doe@example.com");

        verify(userRepository).findByEmailContainingAndRoleName(filter.email(), filter.roleName());
    }

    @Test
    void should_findAndReturnListUsersInfoDTO_byAdministratorRoleFilter() {
        UserFilterDTO filter = new UserFilterDTO(null, RoleName.ADMINISTRATOR);

        when(userRepository.findByEmailContainingAndRoleName(null, RoleName.ADMINISTRATOR))
                .thenReturn(List.of(admin, adminAndUser));

        List<UserDTO> result = userService.findByFilter(filter);

        assertEquals(2, result.size());
        assertEquals("jane.doe@example.com", result.get(0).email());
        assertEquals(RoleName.ADMINISTRATOR, result.get(0).roles().get(0).getName());

        verify(userRepository).findByEmailContainingAndRoleName(filter.email(), filter.roleName());
    }

    @Test
    void should_findAndReturnListUsersInfoDTO_byEmailAndRoleFilter() {
        UserFilterDTO filter = new UserFilterDTO("j", RoleName.USER);

        when(userRepository.findByEmailContainingAndRoleName("j", RoleName.USER))
                .thenReturn(List.of(user, adminAndUser));

        List<UserDTO> result = userService.findByFilter(filter);

        assertEquals(2, result.size());
        assertEquals(user.getEmail(), result.get(0).email());
        assertTrue(result.get(0).roles().stream().anyMatch(role ->
                RoleName.USER.equals(role.getName())));
        assertEquals(adminAndUser.getEmail(), result.get(1).email());
        assertTrue(result.get(1).roles().stream().anyMatch(role ->
                RoleName.USER.equals(role.getName())));

        verify(userRepository).findByEmailContainingAndRoleName(filter.email(), filter.roleName());
    }

    @Test
    void should_findAndReturn_emptyList_whenUserNoMatches_filter() {
        UserFilterDTO filter = new UserFilterDTO("nonexisting", null);

        when(userRepository.findByEmailContainingAndRoleName(filter.email(), filter.roleName()))
                .thenReturn(Collections.emptyList());

        List<UserDTO> result = userService.findByFilter(filter);

        assertTrue(result.isEmpty());

        verify(userRepository).findByEmailContainingAndRoleName(filter.email(), filter.roleName());
    }

    @Test
    void should_find_existingUser_by_email() {
        String email = "john.doe@example.com";

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

    @Test
    void should_findUserByIdAndUpdateEmailAndPasswordAndRole() {
        Long id = 1L;

        UpdateUserDTO dto = new UpdateUserDTO("newemail@example.com", "newpassword", List.of(adminRole));

        String newEncodedPassword = "encondednewpassword";
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(dto.password())).thenReturn(newEncodedPassword);

        userService.update(id, dto);

        assertEquals(1L, user.getId());
        assertEquals(dto.email(), user.getEmail());
        assertEquals(newEncodedPassword, user.getPassword());

        verify(userRepository).findById(id);
        verify(passwordEncoder).encode(dto.password());
    }

    @Test
    void should_throw_resourceNotFoundException_updateNonExistingUser() {
        Long id = 1L;
        UpdateUserDTO dto = new UpdateUserDTO("newemail@example.com", "newpassword", List.of(adminRole));

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> userService.update(id, dto));
    }

    @Test
    void should_deleteUserByValidId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doNothing().when(userRepository).delete(any(User.class));

        userService.delete(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void should_throw_resourceNotFoundException_deleteNonExistingUser() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> userService.delete(id));
    }

}