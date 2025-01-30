package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.GenericIntegrationTestBase;
import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.UpdateUserDTO;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.domain.enumaration.RoleName;
import br.com.gabezy.todoapi.repositories.UserRespository;
import br.com.gabezy.todoapi.utils.AuthenticationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends GenericIntegrationTestBase {

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationUtils authenticationUtils;

    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO("user@example.com", "pass");

    }

    private static final String INSERT_USERS_SCRIPT = "classpath:/scripts/user/insert_users.sql";
    private static final String CLEAN_USERS_SCRIPT = "classpath:/scripts/user/clean_users.sql";


    @Test
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_createAndReturnLocationHeaderWithUriToGetTheUser() throws Exception {
        String createUserRoleSql = "INSERT INTO roles (IDT_ROLE, NAME) VALUES (1, 'USER')";
        jdbcTemplate.execute(createUserRoleSql);

        CreateUserDTO dto = new CreateUserDTO("newuser@email.com", "strongpassword123");

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        MvcResult mvcResult  = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, containsString("/users/")))
                .andReturn();

        String location = mvcResult.getResponse().getHeader(LOCATION);

        assertNotNull(location);

        String[] splittedLocationString = location.split("/");

        Long userId = Long.parseLong(splittedLocationString[splittedLocationString.length - 1]);

        Optional<User> userOptional = userRespository.findById(userId);

        assertTrue(userOptional.isPresent());
        assertNotEquals(dto.password(), userOptional.get().getPassword());
        assertTrue(passwordEncoder.matches(dto.password(), userOptional.get().getPassword()));
    }

    @Test
    void should_throw400BadRequest_whenCreateUserWithInvalidInformation() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("usermaneNotEmail", "");

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        RequestBuilder postRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", aMapWithSize(2)));

    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_findByIdAndReturnUser_whenPassAdministratorToken() throws Exception {
        String token = authenticationUtils.generateTokenForAdministrator(jdbcTemplate, loginDTO).token();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.email", is("jonh.doe@email.com")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_throw403Forbidden_whenFindByIdPassingUserToken() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_findAllAndReturnUsersPage_whenPassAdministratorToken() throws Exception {
        String token = authenticationUtils.generateTokenForAdministrator(jdbcTemplate, loginDTO).token();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/users")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(8)))
                .andExpect(jsonPath("$.totalElements", is(8)));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_findAllAndReturnUsersPageSortedByEmail_whenPassAdministratorToken() throws Exception {
        String token = authenticationUtils.generateTokenForAdministrator(jdbcTemplate, loginDTO).token();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/users")
                .header(AUTHORIZATION, "Bearer " + token)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "email,asc");


        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(8)))
                .andExpect(jsonPath("$.totalElements", is(8)))
                .andExpect(jsonPath("$.content[0].email", is("eminen.doe@email.com")))
                .andExpect(jsonPath("$.content[1].email", is("francisco.doe@email.com")))
                .andExpect(jsonPath("$.content[2].email", is("gabriel.doe@email.com")));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_throw403Forbidden_whenFindAllPassingUserToken() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/users")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_updateAndReturn204NoContentStatusWithUserToken() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("newemail@example.com", "newPassword", List.of(RoleName.ADMINISTRATOR));

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{id}", 1)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());

        Optional<User> userOptional = userRespository.findById(1L);

        assertTrue(userOptional.isPresent());
        assertEquals(updateUserDTO.email(), userOptional.get().getEmail());
        assertTrue(passwordEncoder.matches(updateUserDTO.password(), userOptional.get().getPassword()));
        assertTrue(userOptional.get().getRoles()
                .stream()
                .allMatch(role -> role.getName().equals(RoleName.ADMINISTRATOR)));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_updateDifferentUserAndReturn403ForbiddenStatus() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("newemail@example.com", "newPassword", List.of(RoleName.ADMINISTRATOR));

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO));

        ErrorCode errorCode = ErrorCode.USER_NOT_AUTHORIZED;

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_updateWithInvalidInformationAndReturn400BadRequestStatus() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("newemai", "", List.of());

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO));

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", aMapWithSize(3)));
    }

    @Test
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_updateWithNonExistingIdAndReturn404NotFoudStatus() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("newemail@example.com", "newPassword", List.of(RoleName.ADMINISTRATOR));

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO));

        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_updateDifferentUserAndReturn403ForbiddenStatusWithAdminToken() throws Exception {
        String token = authenticationUtils.generateTokenForAdministrator(jdbcTemplate, loginDTO).token();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("newemail@example.com", "newPassword", List.of(RoleName.ADMINISTRATOR));

        RequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO));

        ErrorCode errorCode = ErrorCode.USER_NOT_AUTHORIZED;

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void should_DeleteAndReturn204NoContentStatusWithUserToken() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        assertTrue(userRespository.findById(1L).isPresent());

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/users/{id}", 1)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());

        assertTrue(userRespository.findById(1L).isEmpty());
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_deleteDifferentUserAndReturn403ForbiddenStatus() throws Exception {
        assertTrue(userRespository.findById(10L).isPresent());

        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token);

        ErrorCode errorCode = ErrorCode.USER_NOT_AUTHORIZED;

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(userRespository.findById(10L).isPresent());
    }

    @Test
    @Sql(scripts = INSERT_USERS_SCRIPT, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_deleteDifferentUserAndReturn403ForbiddenStatusWithAdminToken() throws Exception {
        assertTrue(userRespository.findById(10L).isPresent());

        String token = authenticationUtils.generateTokenForAdministrator(jdbcTemplate, loginDTO).token();

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/users/{id}", 10)
                .header(AUTHORIZATION, "Bearer " + token);

        ErrorCode errorCode = ErrorCode.USER_NOT_AUTHORIZED;

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(userRespository.findById(10L).isPresent());
    }

    @Test
    @Sql(scripts = CLEAN_USERS_SCRIPT, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNot_deleteWithNonExistingIdAndReturn404NotFound() throws Exception {
        String token = authenticationUtils.generateTokenForUser(jdbcTemplate, loginDTO).token();

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/users/{id}", 1000)
                .header(AUTHORIZATION, "Bearer " + token);

        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

    }

}