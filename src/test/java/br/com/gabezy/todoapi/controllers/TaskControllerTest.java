package br.com.gabezy.todoapi.controllers;

import br.com.gabezy.todoapi.GenericTestBase;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import br.com.gabezy.todoapi.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskControllerTest extends GenericTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRespository taskRespository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String INSERT_TASK_SCRIPT = "classpath:/scripts/task/insert_task.sql";

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

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", 1000))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.code", any(String.class)))
                .andExpect(jsonPath("$.description", any(String.class)))
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

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, containsString("tasks/1")));

        assertTrue(taskRespository.findById(1L).isPresent());
    }

    // TODO: pass test
    @Test
    void should_throw_400_badRequest_post_invalidTask() throws Exception {
        assertTrue(taskRespository.findById(1L).isEmpty());

        TaskDTO task = new TaskDTO("", null);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.INVALID_FIELDS.name())))
                .andExpect(jsonPath("$.description", any(String.class)))
                .andExpect(jsonPath("$.fields", any(Map.class)));


        assertTrue(taskRespository.findById(1L).isEmpty());
    }

    @AfterEach
    void clean() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "TASK");
    }

    private void insertTaskWithIdOne(String content, Boolean completed) {
        int completedStatus = completed.equals(Boolean.TRUE) ? 1 : 0;
        String sql = String.format("INSERT INTO TASK (IDT_TASK, CONTENT, COMPLETED) VALUES (1, '%s', %d)", content, completedStatus);
        jdbcTemplate.execute(sql);
    }

}