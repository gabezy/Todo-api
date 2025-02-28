package br.com.gabezy.todoapi.utils;

import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.TokenDTO;
import br.com.gabezy.todoapi.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.jdbc.JdbcTestUtils;

@Component
public class AuthenticationUtils {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    public TokenDTO generateTokenForUser(JdbcTemplate jdbcTemplate, LoginDTO loginDTO) {
        String encodedPassword = passwordEncoder.encode(loginDTO.password());

        String insertRoleSql = "INSERT INTO roles (IDT_ROLE, NAME) " +
                "SELECT 1, 'USER' " +
                "WHERE NOT EXISTS(" +
                    "SELECT 1 FROM roles " +
                    "WHERE IDT_ROLE = 1 OR NAME = 'USER'" +
                ")";

        String insertUserSql = String.format("INSERT INTO users (IDT_USER, EMAIL, PASSWORD, CREATED_AT) " +
                "VALUES (1, '%s', '%s', CURRENT_TIMESTAMP)", loginDTO.email(), encodedPassword);

        String insertUserRoleSql = "INSERT INTO user_role (IDT_USER, IDT_ROLE) VALUES (1, 1)";

        jdbcTemplate.execute(insertRoleSql);
        jdbcTemplate.execute(insertUserSql);
        jdbcTemplate.execute(insertUserRoleSql);

        return authenticationService.authenticate(loginDTO);
    }

    public TokenDTO generateTokenForAdministrator(JdbcTemplate jdbcTemplate, LoginDTO loginDTO) {
        String encodedPassword = passwordEncoder.encode(loginDTO.password());

        String insertRoleSql = "INSERT INTO roles (IDT_ROLE, NAME) " +
                "SELECT 1, 'ADMINISTRATOR' " +
                "WHERE NOT EXISTS(" +
                "SELECT 1 FROM roles " +
                "WHERE IDT_ROLE = 2 OR NAME = 'ADMINISTRATOR'" +
                ")";

        String insertUserSql = String.format("INSERT INTO users (IDT_USER, EMAIL, PASSWORD, CREATED_AT) " +
                "VALUES (2, '%s', '%s', CURRENT_TIMESTAMP)", loginDTO.email(), encodedPassword);

        String insertUserRoleSql = "INSERT INTO user_role (IDT_USER, IDT_ROLE) VALUES (2, 2)";

        jdbcTemplate.execute(insertRoleSql);
        jdbcTemplate.execute(insertUserSql);
        jdbcTemplate.execute(insertUserRoleSql);

        return authenticationService.authenticate(loginDTO);
    }

    public void cleanUpAssistantTables(JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_role", "users", "roles");
    }

}
