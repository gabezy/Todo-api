package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UpdateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UserDTO;
import br.com.gabezy.todoapi.domain.dto.UserFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import br.com.gabezy.todoapi.exceptions.InvalidCredentialsException;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.UserRespository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRespository repository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRespository repository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserDTO dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(getUserRole());
        return repository.save(user);
    }

    public UserDTO findById(Long id) {
        User user = this.findUserById(id);
        return mapToUserInfoDTO(user);
    }

    public List<UserDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapToUserInfoDTO)
                .toList();
    }

    public List<UserDTO> findByFilter(UserFilterDTO dto) {
        return repository.findByEmailContainingAndRoleName(dto.email(), dto.roleName())
                .stream()
                .map(this::mapToUserInfoDTO)
                .toList();
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    public void update(Long id, UpdateUserDTO dto) {
        User user = this.findUserById(id);

        validateUserAccess(user);

        List<Role> roles = getRolesByRoleNames(dto.roles());
        String endocodedPassword = passwordEncoder.encode(dto.password());

        user.setEmail(dto.email());
        user.setPassword(endocodedPassword);

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        repository.save(user);
    }

    public void delete(Long id) {
        User user = this.findUserById(id);
        validateUserAccess(user);
        repository.delete(user);
    }

    private User findUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private List<Role> getUserRole() {
        return List.of(roleService.findByName(RoleName.USER));
    }

    private UserDTO mapToUserInfoDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }

    private List<Role> getRolesByRoleNames(List<RoleName> roleNames) {
        return roleNames.stream()
                .map(roleService::findByName)
                .toList();
    }

    private void validateUserAccess(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException(ErrorCode.USER_NOT_AUTHENTICATED);
        }

        String authenticadedUserEmail = (String) authentication.getPrincipal();

        if (!authenticadedUserEmail.equals(user.getEmail())) {
            throw new InvalidCredentialsException(ErrorCode.USER_NOT_AUTHORIZED);
        }

    }

}
