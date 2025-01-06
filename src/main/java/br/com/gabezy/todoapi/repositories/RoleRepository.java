package br.com.gabezy.todoapi.repositories;

import br.com.gabezy.todoapi.domain.entity.Role;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

}
