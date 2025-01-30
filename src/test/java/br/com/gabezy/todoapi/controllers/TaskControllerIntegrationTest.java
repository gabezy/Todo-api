package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.GenericIntegrationTestBase;
import br.com.gabezy.todoapi.domain.dto.CreateUserDTO;
import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import br.com.gabezy.todoapi.services.AuthenticationService;
import br.com.gabezy.todoapi.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "classpath:/scripts/task/clean_task.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TaskControllerIntegrationTest extends GenericIntegrationTestBase {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskRespository taskRespository;

    private String token;

    private static final String INSERT_TASKS_SCRIPT = "src/test/resources/scripts/task/insert_task.sql";

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("INSERT INTO roles (IDT_ROLE, NAME) VALUES (1, 'USER')");

        Long user1Id = userService.createUser(new CreateUserDTO("jonh.doe@example.com", "password")).getId();

        Long user2Id = userService.createUser(new CreateUserDTO("jane.doe@example.com", "password")).getId();

        LoginDTO loginDTO = new LoginDTO("jonh.doe@example.com", "password");

        token = authenticationService.authenticate(loginDTO).token();

        String tasksSql = Files.readString(Paths.get(INSERT_TASKS_SCRIPT))
                .replace("{USER_ID_1}", user1Id.toString())
                .replace("{USER_ID_2}", user2Id.toString());

        jdbcTemplate.execute(tasksSql);

    }

    @Test
    void should_getAndReturnTaskExistingByIdOwnedByLoggedUser() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/{id}", 2)
                        .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.content", is("Learn Docker")))
                .andExpect(jsonPath("$.completed", is(Boolean.FALSE)));
    }

    @Test
    void should_return404NotFound_whenFindByIdOtherUserTask() throws Exception {
        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/{id}", 3)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(3L).isPresent());
    }

    @Test
    void should_return401Unauthorized_whenFindById_withoutAuthorizationHeader() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/{id}", 3);

        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void should_return401Unauthorized_whenFindById_withoutAValidToken() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnYWJlenktdG9kby1hcGkiLCJpYXQiOjE3Mzc4MzAyNjksImV4cCI6MTczNzg0NDY2OSwic3ViIjoiYWRtaW5AZW1haWwuY29tIn0";

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/{id}", 3)
                .header(AUTHORIZATION, "Bearer " + invalidToken);

        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_404_notFound_get_nonExisting_task() throws Exception {
        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/{id}", 1000)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertFalse(taskRespository.findById(1000L).isPresent());
    }

    @Test
    void should_getAndReturnAllTaskPageFromLoggedUser() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void should_getAndReturnAllTaskPageFromLoggedUserSortedByContent() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks")
                .header(AUTHORIZATION, "Bearer " + token)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "content,asc");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.content[0].content", is("Fix jira issue #211321")))
                .andExpect(jsonPath("$.content[1].content", is("Fix jira issue #front-end123213")))
                .andExpect(jsonPath("$.content[2].content", is("Learn Docker")))
                .andExpect(jsonPath("$.content[3].content", is("Talk to the infrastructure guy by the server issue")));
    }

    @Test
    void should_getAndReturnListOfTasksLoggedUserWithCompletedEqualsTrue() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/filter")
                .queryParam("completed", "true")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].content", everyItem(any(String.class))))
                .andExpect(jsonPath("$[*].completed", everyItem(is(Boolean.TRUE))));
    }

    @Test
    void should_getAndReturnListOfTasksLoggedUserWithContentEqualsTrue() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/filter")
                .queryParam("content", "learn")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].content", everyItem(containsStringIgnoringCase("learn"))))
                .andExpect(jsonPath("$[*].completed", everyItem(any(Boolean.class))));
    }
    @Test
    void should_getAndReturnListOfTasksLoggedUser_withCompletedEqualsTrueAndContentEqualsIssue() throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get("/tasks/filter")
                .queryParam("completed", "true")
                .queryParam("content", "issue")
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].content", everyItem(containsStringIgnoringCase("issue"))))
                .andExpect(jsonPath("$[*].completed", everyItem(is(Boolean.TRUE))));
    }

    @Test
    void should_create_a_new_task() throws Exception {
        TaskDTO task = new TaskDTO("Learn spring boot", Boolean.TRUE);

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/tasks")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, containsString("tasks/1")));

    }

    @Test
    void should_throw_400_badRequest_post_invalid_data_task() throws Exception {
        TaskDTO task = new TaskDTO("", null);

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/tasks")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", any(Map.class)));

        assertTrue(taskRespository.findById(1L).isEmpty());
    }

    @Test
    void should_notUpdateExistingTask() throws Exception {
        TaskDTO taskUpdated = new TaskDTO("Learn Azure", Boolean.FALSE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 2)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        mockMvc.perform(putRequestBuilder).andExpect(status().isNoContent());
    }

    @Test
    void should_notUpdateExistingTaskFromOtherUser_andReturn404NotFoundStatus() throws Exception {
        TaskDTO taskUpdated = new TaskDTO("Learn Azure", Boolean.FALSE);

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 3)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        mockMvc.perform(putRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    void should_throw400BadRequestUpdateInvalidDataTask() throws Exception {
        TaskDTO taskUpdated = new TaskDTO("", Boolean.TRUE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        mockMvc.perform(putRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", any(Map.class)));
    }

    @Test
    void should_throw404NotFoundUpdateNonExistingTask() throws Exception {
        TaskDTO taskUpdated = new TaskDTO("Study to OCP Java 21", Boolean.TRUE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(putRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    void should_changeCompletedStatusFromExistingTaskOwnedByLoggedUser() throws Exception{
        TaskCompletedDTO dto = new TaskCompletedDTO(Boolean.TRUE);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 2)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(patchRequestBuilder).andExpect(status().isNoContent());
    }

    @Test
    void should_throw400BadRequestPatchInvalidDataTask() throws Exception {
        TaskCompletedDTO dto = new TaskCompletedDTO(null);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 1)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        ErrorCode errorCode = ErrorCode.INVALID_FIELDS;

        mockMvc.perform(patchRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", any(Map.class)));
    }

    @Test
    void should_throw404NotFound_whenPatchNonExistingTask() throws Exception {
        TaskCompletedDTO dto = new TaskCompletedDTO(Boolean.FALSE);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 1000)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(patchRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(1000L).isEmpty());
    }

    @Test
    void should_throw404NotFound_whenPatchExistingTaskFromOtherUser() throws Exception {
        TaskCompletedDTO dto = new TaskCompletedDTO(Boolean.FALSE);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 3)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(patchRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(3L).isPresent());
    }

    @Test
    void should_deleteExistingTaskOwnedByLoggedUser() throws Exception {
        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/tasks/{id}", 2)
                        .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(deleteRequest).andExpect(status().isNoContent());

        assertTrue(taskRespository.findById(2L).isEmpty());
    }

    @Test
    void should_return404NotFound_whenDeleteNonExistingTaskOwnedByLoggedUser() throws Exception {
        Long invalidId = 1000L;

        assertTrue(taskRespository.findById(invalidId).isEmpty());

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/tasks/{id}", invalidId)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    void should_return404NotFound_whenDeleteExistingTaskFromOtherUser() throws Exception {
        Long otherUserTaskId = 3L;

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/tasks/{id}", otherUserTaskId)
                .header(AUTHORIZATION, "Bearer " + token);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(otherUserTaskId).isPresent());
    }

}