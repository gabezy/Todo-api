package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.GenericIntegrationTestBase;
import br.com.gabezy.todoapi.domain.dto.LoginDTO;
import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import br.com.gabezy.todoapi.services.TaskService;
import br.com.gabezy.todoapi.utils.AuthenticationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskControllerIntegrationTest extends GenericIntegrationTestBase {

    @Autowired
    private AuthenticationUtils authenticationUtils;

    @Autowired
    private TaskRespository taskRespository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskService taskService;

    private static final String INSERT_TASK_SCRIPT = "classpath:/scripts/task/insert_task.sql";

    @BeforeEach
    void setUp() {
        LoginDTO loginDTO = new LoginDTO("jonh.doe@example.com", "password");

        String token = authenticationUtils.generateToken(jdbcTemplate, loginDTO).token();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(MockMvcRequestBuilders.request(HttpMethod.GET, "/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .build();
    }

    @Test
    void should_return_a_task_existing_by_id() throws Exception {
        insertTaskWithIdOne("Study Laravel", Boolean.TRUE);

        assertTrue(taskRespository.findById(1L).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("Study Laravel")))
                .andExpect(jsonPath("$.completed", is(Boolean.TRUE)));

    }

    @Test
    void should_return_404_notFound_get_nonExisting_task() throws Exception {
        assertFalse(taskRespository.findById(1000L).isPresent());

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", 1000))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_return_list_of_task() throws Exception {
        assertTrue(taskRespository.findAll().iterator().hasNext());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_return_list_of_task_by_filter() throws Exception {
        var filter = new TaskFilterDTO(null, Boolean.TRUE);

        List<Task> tasksFiltered = taskService.findByFilter(filter);

        assertFalse(tasksFiltered.isEmpty());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/filter?completed=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(tasksFiltered.size())))
                .andExpect(jsonPath("$[0].content", any(String.class)))
                .andExpect(jsonPath("$[0].completed", is(Boolean.TRUE)));

        filter = new TaskFilterDTO("learn", null);

        tasksFiltered = taskService.findByFilter(filter);

        assertFalse(tasksFiltered.isEmpty());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/filter?content=learn"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(tasksFiltered.size())))
                .andExpect(jsonPath("$[0].content", containsStringIgnoringCase("learn")))
                .andExpect(jsonPath("$[0].completed", any(Boolean.class)));

        filter = new TaskFilterDTO("java", Boolean.TRUE);

        tasksFiltered = taskService.findByFilter(filter);

        assertFalse(tasksFiltered.isEmpty());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/filter?content=java&completed=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(tasksFiltered.size())))
                .andExpect(jsonPath("$[0].content", containsStringIgnoringCase("java")))
                .andExpect(jsonPath("$[0].completed", is(Boolean.TRUE)));
    }

    @Test
    void should_create_a_new_task() throws Exception {
        assertTrue(taskRespository.findById(1L).isEmpty());

        TaskDTO task = new TaskDTO("Learn spring boot", Boolean.TRUE);

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(postRequestBuilder)
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, containsString("tasks/1")));

        assertTrue(taskRespository.findById(1L).isPresent());
    }

    @Test
    void should_throw_400_badRequest_post_invalid_data_task() throws Exception {
        TaskDTO task = new TaskDTO("", null);

        RequestBuilder postRequestBuilder = MockMvcRequestBuilders.post("/tasks")
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
    void should_update_a_existing_task() throws Exception {
        insertTaskWithIdOne("Learn AWS", Boolean.FALSE);

        TaskDTO taskUpdated = new TaskDTO("Learn Azure", Boolean.FALSE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        mockMvc.perform(putRequestBuilder).andExpect(status().isNoContent());

        Task task = taskService.findById(1L);

        assertEquals(1L, task.getId());
        assertEquals(taskUpdated.content(), task.getContent());
        assertEquals(taskUpdated.completed(), task.getCompleted());
    }

    @Test
    void should_throw_400_badRequest_update_invalid_data_task() throws Exception {
        insertTaskWithIdOne("Learn AWS", Boolean.FALSE);

        TaskDTO taskUpdated = new TaskDTO("", Boolean.TRUE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 1)
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
    void should_throw_404_notFound_update_nonExisting_task() throws Exception {
        TaskDTO taskUpdated = new TaskDTO("Study to OCP Java 21", Boolean.TRUE);

        RequestBuilder putRequestBuilder = MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdated));

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(putRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(1L).isEmpty());
    }

    @Test
    void should_change_completedStatus_from_a_existing_task() throws Exception{
        insertTaskWithIdOne("Study Math", Boolean.FALSE);

        TaskCompletedDTO dto = new TaskCompletedDTO(Boolean.TRUE);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(patchRequestBuilder).andExpect(status().isNoContent());

        assertEquals(Boolean.TRUE, taskService.findById(1L).getCompleted());
    }

    @Test
    void should_throw_400_badRequest_patch_invalid_data_task() throws Exception {
        insertTaskWithIdOne("Learn AWS", Boolean.FALSE);

        TaskCompletedDTO dto = new TaskCompletedDTO(null);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 1)
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
    void should_throw_404_notFound_patch_nonExisting_task() throws Exception {
        TaskCompletedDTO dto = new TaskCompletedDTO(Boolean.FALSE);

        RequestBuilder patchRequestBuilder = MockMvcRequestBuilders.patch("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(patchRequestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));

        assertTrue(taskRespository.findById(1L).isEmpty());
    }

    @Test
    void should_delete_a_task() throws Exception {
        insertTaskWithIdOne("Will be delete", Boolean.TRUE);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/{id}", 1))
                .andExpect(status().isNoContent());

        assertTrue(taskRespository.findById(1L).isEmpty());
    }

    @Test
    void should_return_204_notContent_delete_nonExisting_task() throws Exception {
        Long invalidId = 1000L;

        assertTrue(taskRespository.findById(invalidId).isEmpty());

        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.description", is(errorCode.getMessage())))
                .andExpect(jsonPath("$.fields", anEmptyMap()));
    }

    @AfterEach
    void clean() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "tasks");
        authenticationUtils.cleanUpAssistantTables(jdbcTemplate);
    }

    private void insertTaskWithIdOne(String content, Boolean completed) {
        int completedStatus = completed.equals(Boolean.TRUE) ? 1 : 0;
        String sql = String.format("INSERT INTO tasks (IDT_TASK, CONTENT, COMPLETED) VALUES (1, '%s', %d)", content, completedStatus);
        jdbcTemplate.execute(sql);
    }

}