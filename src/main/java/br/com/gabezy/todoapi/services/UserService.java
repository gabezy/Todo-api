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
import br.com.gabezy.todoapi.utils.AuthenticationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Page<UserDTO> findAll(Pageable pageable) {
        Page<User> userPage = repository.findAll(pageable);

        List<UserDTO> userDTOList = userPage.getContent()
                .stream()
                .map(this::mapToUserInfoDTO)
                .toList();

        return new PageImpl<>(userDTOList, pageable, userPage.getTotalElements());
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
        String currentUserEmail = AuthenticationUtil.getCurrentUserEmail();
        if (!user.getEmail().equals(currentUserEmail)) {
            throw new InvalidCredentialsException(ErrorCode.USER_NOT_AUTHORIZED);
        }
    }

}
