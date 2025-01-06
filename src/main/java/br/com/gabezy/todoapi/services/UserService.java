package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.UserInfoDTO;
import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.UserRespository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRespository respository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRespository respository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.respository = respository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserDTO dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(getUserRole());
        return respository.save(user);
    }

    public UserInfoDTO findById(Long id) {
        var user = respository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return maptoUserInfoDTO(user);
    }

    public User findByEmail(String email) {
        return respository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private List<Role> getUserRole() {
        return List.of(roleService.findByName(RoleName.USER));
    }

    private UserInfoDTO maptoUserInfoDTO(User user) {
        return new UserInfoDTO(user.getId(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }

}
