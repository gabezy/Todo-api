package br.com.gabezy.todoapi.repositories;

import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRespository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u " +
            "JOIN u.roles r " +
            "WHERE LOWER(u.email) LIKE CONCAT('%', LOWER(:email), '%') " +
            "OR r.name = :roleName"
    )
    List<User> findByEmailOrRoleName(String email, RoleName roleName);

}
