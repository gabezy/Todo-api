package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.GenericIntegrationTestBase;
import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerIntegrationTest extends GenericIntegrationTestBase {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        String encodedPassword = passwordEncoder.encode("password123");

        String insertRoleSql = "INSERT INTO roles (IDT_ROLE, NAME) VALUES (1, 'USER')";
        String insertUserSql = String.format("INSERT INTO users (IDT_USER, EMAIL, PASSWORD, CREATED_AT) " +
                "VALUES (1, 'jonh.doe@example.com', '%s', CURRENT_TIMESTAMP)", encodedPassword);
        String insertUserRoleSql = "INSERT INTO user_role (IDT_USER, IDT_ROLE) VALUES (1, 1)";

        jdbcTemplate.execute(insertRoleSql);
        jdbcTemplate.execute(insertUserSql);
        jdbcTemplate.execute(insertUserRoleSql);
    }

    @Test
    void should_authenticate_valid_user() throws Exception {
        LoginDTO loginDTO = new LoginDTO("jonh.doe@example.com", "password123");

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/auth")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO));

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", any(String.class)));
    }

    @Test
    void shouldNot_authenticate_notMatchPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO("jonh.doe@example.com", "password12");

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/auth")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO));

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldThrow_404UserNotFound_when_authenticateWithNotMatchEmail() throws Exception {
        LoginDTO loginDTO = new LoginDTO("jane.doe@example.com", "password123");

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/auth")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO));

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isForbidden());
    }

    @Test
    void should_response_400BadRequest_whenAuthenticateWithBadCredentials() throws Exception {
        LoginDTO loginDTO = new LoginDTO("jonh.doe@example.com", "");

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/auth")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO));

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", any(Map.class)));
    }

    @AfterEach
    void cleanUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_role", "users", "roles");
    }
}