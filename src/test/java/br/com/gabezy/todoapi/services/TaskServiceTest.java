package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRespository taskRespository;

    @InjectMocks
    private TaskService taskService;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = new Task();
        task1.setId(1L);
        task1.setContent("Learn JUnit 5");
        task1.setCompleted(Boolean.FALSE);

        task2 = new Task();
        task2.setId(2L);
        task2.setContent("learn Docker");
        task2.setCompleted(Boolean.TRUE);

        task3 = new Task();
        task2.setId(3L);
        task3.setContent("Deploy TODO app");
        task3.setCompleted(Boolean.FALSE);
    }


    @Test
    void should_create_new_task() {
        when(taskRespository.save(any(Task.class))).thenReturn(task1);

        TaskDTO newTask = new TaskDTO("Learn JUnit 5", Boolean.FALSE);

        Task result = taskService.createTask(newTask);

        assertNotNull(result);
        assertEquals(task1.getContent(), result.getContent());
        assertEquals(task1.getCompleted(), result.getCompleted());

        verify(taskRespository).save(any(Task.class));
    }

    @Test
    void should_find_all_tasks() {
        when(taskRespository.findAll()).thenReturn(List.of(task1, task2));

        List<Task> result = taskService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(task -> task.getCompleted().equals(Boolean.TRUE)));
        assertTrue(result.stream().anyMatch(task ->task.getCompleted().equals(Boolean.FALSE)));

        verify(taskRespository).findAll();
    }

    @Test
    void should_find_task_by_id() {
        Long id = 1L;

        when(taskRespository.findById(id)).thenReturn(Optional.of(task1));

        Task result = taskService.findById(id);

        assertNotNull(result);
        assertEquals(task1.getContent(), result.getContent());
        assertEquals(task1.getCompleted(), result.getCompleted());

        verify(taskRespository).findById(id);
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_task_by_invalid_id() {
        Long id = 1000L;

        when(taskRespository.findById(id)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.findById(id));

        verify(taskRespository).findById(id);
    }

    @Test
    void should_findAndReturnListTasks_byPartialContentFilter() {
        TaskFilterDTO filter = new TaskFilterDTO("Learn", null);

        when(taskRespository.findTaskByContentContainingAndCompleted(filter.content(), filter.completed()))
                .thenReturn(List.of(task1, task2));

        List<Task> result = taskService.findByFilter(filter);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(task -> task.getCompleted().equals(Boolean.TRUE)));
        assertTrue(result.stream().anyMatch(task ->task.getCompleted().equals(Boolean.FALSE)));

        verify(taskRespository).findTaskByContentContainingAndCompleted(filter.content(), filter.completed());
    }

    @Test
    void should_findAndReturnListTasks_byPartialCompletedTrueFilter() {
        TaskFilterDTO filter = new TaskFilterDTO(null, Boolean.TRUE);

        when(taskRespository.findTaskByContentContainingAndCompleted(filter.content(), filter.completed()))
                .thenReturn(List.of(task2));

        List<Task> result = taskService.findByFilter(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(task -> task.getCompleted().equals(Boolean.TRUE)));
        assertTrue(result.stream().anyMatch(task -> task.getContent().equals(task2.getContent())));

        verify(taskRespository).findTaskByContentContainingAndCompleted(filter.content(), filter.completed());
    }

    @Test
    void should_findAndReturnListTasks_byPartialCompletedFalseFilter() {
        TaskFilterDTO filter = new TaskFilterDTO(null, Boolean.FALSE);

        when(taskRespository.findTaskByContentContainingAndCompleted(filter.content(), filter.completed()))
                .thenReturn(List.of(task1, task3));

        List<Task> result = taskService.findByFilter(filter);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(task -> task.getCompleted().equals(Boolean.FALSE)));
        assertTrue(result.stream().anyMatch(task -> task.getContent().equals(task1.getContent())));
        assertTrue(result.stream().anyMatch(task -> task.getContent().equals(task3.getContent())));

        verify(taskRespository).findTaskByContentContainingAndCompleted(filter.content(), filter.completed());
    }

    @Test
    void should_update_existing_task() {

        when(taskRespository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskRespository.save(any())).thenReturn(mock(Task.class));

        TaskDTO taskUpdate = new TaskDTO("Migrate system to Spring boot", Boolean.FALSE);

        taskService.updateTask(1L, taskUpdate);

        assertEquals(taskUpdate.content(), task1.getContent());
        assertEquals(taskUpdate.completed(), task1.getCompleted());
        verify(taskRespository).findById(1L);
        verify(taskRespository).save(any(Task.class));
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_update_task_by_invalid_id() {
        Long invalidId = 1L;

        when(taskRespository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.updateTask(invalidId, new TaskDTO("some content", Boolean.FALSE)));

        verify(taskRespository).findById(invalidId);
    }

    @Test
    void should_change_task_completedStatus() {
        when(taskRespository.findById(anyLong())).thenReturn(Optional.of(task1), Optional.of(task2));

        when(taskRespository.save(any(Task.class))).thenReturn(mock(Task.class), mock(Task.class));

        taskService.patchCompletedStatus(1L, new TaskCompletedDTO(Boolean.TRUE));
        taskService.patchCompletedStatus(2L, new TaskCompletedDTO(Boolean.FALSE));

        assertEquals(Boolean.TRUE, task1.getCompleted());
        assertEquals(Boolean.FALSE, task2.getCompleted());

        verify(taskRespository, times(2)).findById(anyLong());
        verify(taskRespository, times(2)).save(any(Task.class));
    }

    @Test
    void should_throw_resourceNotFoundException_when_change_task_completedStatus_by_invalid_id() {
        Long invalidId = 1000L;

        when(taskRespository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.patchCompletedStatus(invalidId, new TaskCompletedDTO(Boolean.FALSE)));
    }

    @Test
    void should_delete_task_by_id() {
        when(taskRespository.findById(1L)).thenReturn(Optional.of(task1));
        doNothing().when(taskRespository).delete(task1);

        taskService.deleteTaskById(1L);

        verify(taskRespository).findById(1L);
        verify(taskRespository).delete(task1);
    }

    @Test
    void should_throw_resourceNotFoundException_when_delete_task_completedStatus_by_nonExisting_id() {
        Long invalidId = 1000L;

        when(taskRespository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrowsExactly(ResourceNotFoundException.class,
                () -> taskService.deleteTaskById(invalidId));
    }

}