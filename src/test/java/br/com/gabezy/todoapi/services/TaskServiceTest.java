package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.GenericTestBase;
import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest extends GenericTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRespository taskRespository;

    @Autowired
    private TaskService taskService;

    private static final String INSERT_TASK_SCRIPT = "classpath:/scripts/task/insert_task.sql";

    @Test
    void should_create_new_task() {
        assertFalse(taskRespository.findById(1L).isPresent());

        TaskDTO newTask = new TaskDTO("Learn Spring Boot", Boolean.FALSE);

        Task task = taskService.createTask(newTask);

        var taskOptional = taskRespository.findById(1L);

        assertTrue(taskOptional.isPresent());
        assertEquals(taskOptional.get().getContent(), task.getContent());
        assertEquals(taskOptional.get().getCompleted(), task.getCompleted());
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_find_all_tasks() {
        List<Task> tasks = taskService.findAll();
        List<Task> tasksPersisted = taskRespository.findAll();

        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());

        for (int i = 0; i < tasks.size(); i++) {
            assertEquals(tasksPersisted.get(i).getId(), tasks.get(i).getId());
            assertEquals(tasksPersisted.get(i).getContent(), tasks.get(i).getContent());
            assertEquals(tasksPersisted.get(i).getCompleted(), tasks.get(i).getCompleted());
        }
    }

    @Test
    void should_find_task_by_id() {
        insertTaskWithIdOne("Learn Spring Data", Boolean.TRUE);

        Task task = taskService.findById(1L);

        assertNotNull(task);
        assertEquals(Boolean.TRUE, task.getCompleted());
        assertEquals("Learn Spring Data", task.getContent());
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_task_by_invalid_id() {
        Long invalidId = 1000L;
        assertFalse(taskRespository.findById(invalidId).isPresent());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.findById(invalidId));
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_find_tasks_by_filter() {
        TaskFilterDTO taskFilter = new TaskFilterDTO("Learn", Boolean.FALSE);

        List<Task> tasks = taskRespository.findByContentOrCompleted(taskFilter.content(), taskFilter.completed());
        List<Task> tasksFiltered = taskService.findByFilter(taskFilter);

        taskFilter = new TaskFilterDTO("jira", null);

        List<Task> tasks2 = taskRespository.findByContentOrCompleted(taskFilter.content(), taskFilter.completed());
        List<Task> tasksFiltered2 = taskService.findByFilter(taskFilter);

        taskFilter = new TaskFilterDTO(null, Boolean.TRUE);

        List<Task> tasks3 = taskRespository.findByContentOrCompleted(taskFilter.content(), taskFilter.completed());
        List<Task> tasksFiltered3 = taskService.findByFilter(taskFilter);

        assertAll(
                () -> assertFalse(tasks.isEmpty()),
                () -> assertFalse(tasksFiltered.isEmpty()),
                () -> assertEquals(tasks.size(), tasksFiltered.size()),
                () -> assertEquals(6, tasks.size()),
                () -> assertEquals(6, tasksFiltered.size()),
                () -> assertEquals(1L, tasksFiltered.get(0).getId()),
                () -> assertEquals("Learn Docker", tasksFiltered.get(0).getContent()),
                () -> assertEquals(Boolean.FALSE, tasksFiltered.get(0).getCompleted()),
                //
                () -> assertFalse(tasks2.isEmpty()),
                () -> assertFalse(tasksFiltered2.isEmpty()),
                () -> assertEquals(2, tasks2.size()),
                () -> assertEquals(2, tasksFiltered2.size()),
                () -> assertEquals(3L, tasksFiltered2.get(0).getId()),
                () -> assertEquals("Fix jira issue #211321", tasksFiltered2.get(0).getContent()),
                () -> assertEquals(Boolean.TRUE, tasksFiltered2.get(0).getCompleted()),
                //
                () -> assertFalse(tasks3.isEmpty()),
                () -> assertFalse(tasksFiltered3.isEmpty()),
                () -> assertEquals(2, tasks3.size()),
                () -> assertEquals(2, tasksFiltered3.size()),
                () -> assertEquals(2L, tasksFiltered3.get(0).getId()),
                () -> assertEquals("Learn Java", tasksFiltered3.get(0).getContent()),
                () -> assertEquals(Boolean.TRUE, tasksFiltered3.get(0).getCompleted())
        );
    }

    @Test
    void should_update_existing_task() {
        insertTaskWithIdOne("Learn Spring Data", Boolean.TRUE);

        assertTrue(taskRespository.findById(1L).isPresent());

        TaskDTO taskUpdate = new TaskDTO("Migrate system to Spring boot", Boolean.FALSE);

        taskService.updateTask(1L, taskUpdate);

        Task task = taskService.findById(1L);

        assertEquals(taskUpdate.content(), task.getContent());
        assertEquals(taskUpdate.completed(), task.getCompleted());
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_update_task_by_invalid_id() {
        Long invalidId = 1L;
        assertFalse(taskRespository.findById(invalidId).isPresent());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.updateTask(invalidId, new TaskDTO("some content", Boolean.FALSE)));
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_change_task_completedStatus() {
        Optional<Task> task1 = taskRespository.findById(1L);
        Optional<Task> task2 = taskRespository.findById(2L);

        assertTrue(task1.isPresent());
        assertEquals(Boolean.FALSE, task1.get().getCompleted());

        assertTrue(task2.isPresent());
        assertEquals(Boolean.TRUE, task2.get().getCompleted());

        taskService.patchCompletedStatus(1L, new TaskCompletedDTO(Boolean.TRUE));
        taskService.patchCompletedStatus(2L, new TaskCompletedDTO(Boolean.FALSE));

        assertEquals(Boolean.TRUE, taskService.findById(1L).getCompleted());
        assertEquals(Boolean.FALSE, taskService.findById(2L).getCompleted());

        taskService.patchCompletedStatus(1L, new TaskCompletedDTO(Boolean.TRUE));
        assertEquals(Boolean.TRUE, taskService.findById(1L).getCompleted());
    }

    @Test
    @Sql(scripts = INSERT_TASK_SCRIPT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_throw_resourceNotFoundException_when_change_task_completedStatus_by_invalid_id() {
        Long invalidId = 1000L;
        Optional<Task> task1 = taskRespository.findById(invalidId);

        assertFalse(task1.isPresent());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.patchCompletedStatus(invalidId, new TaskCompletedDTO(Boolean.FALSE)));
    }

    @Test
    void should_delete_task_by_id() {
        insertTaskWithIdOne("some task to do", Boolean.FALSE);

        assertTrue(taskRespository.findById(1L).isPresent());

        taskService.deleteTask(1L);

        assertFalse(taskRespository.findById(1L).isPresent());

        assertFalse(taskRespository.findById(1000L).isPresent());

        taskService.deleteTask(1000L);
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