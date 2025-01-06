package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void should_find_roleByName() {
        var roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setName(RoleName.ADMINISTRATOR);

        when(roleRepository.findByName(RoleName.ADMINISTRATOR))
                .thenReturn(Optional.of(roleAdmin));

        var role = roleService.findByName(RoleName.ADMINISTRATOR)        ;

        assertNotNull(role);
        assertEquals(RoleName.ADMINISTRATOR, role.getName());
    }

    @Test
    void should_throw_resourceNotFoundException_find_invalid_roleName() {
        when(roleRepository.findByName(any(RoleName.class)))
                .thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> roleService.findByName(RoleName.USER));
    }
}