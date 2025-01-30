package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDataDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import br.com.gabezy.todoapi.utils.AuthenticationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private User user;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@exampla.com");
        user.setPassword("password");

        task1 = new Task();
        task1.setId(1L);
        task1.setContent("Learn JUnit 5");
        task1.setCompleted(Boolean.FALSE);

        task2 = new Task();
        task2.setId(2L);
        task2.setContent("learn Docker");
        task2.setCompleted(Boolean.TRUE);

        task3 = new Task();
        task3.setId(3L);
        task3.setContent("Deploy TODO app");
        task3.setCompleted(Boolean.FALSE);
    }


    @Test
    void should_create_new_task() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            when(taskRespository.save(any(Task.class))).thenReturn(task1);
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            TaskDTO newTask = new TaskDTO("Learn JUnit 5", Boolean.FALSE);

            Task result = taskService.createTask(newTask);

            assertNotNull(result);
            assertEquals(task1.getContent(), result.getContent());
            assertEquals(task1.getCompleted(), result.getCompleted());

            verify(taskRespository).save(any(Task.class));
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_findAllTasks_andReturnUserDataDTOPage() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            Pageable pageable = PageRequest.of(0, 5);
            List<Task> tasks = List.of(task1, task2);
            Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
            when(taskRespository.findAllByUser(user, pageable)).thenReturn(taskPage);


            Page<TaskDataDTO> result = taskService.findAll(pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertTrue(result.getContent().stream().anyMatch(task -> task.completed().equals(Boolean.TRUE)));
            assertTrue(result.getContent().stream().anyMatch(task ->task.completed().equals(Boolean.FALSE)));
            assertFalse(result.getContent().stream().anyMatch(task -> task.id().equals(3L)));

            verify(taskRespository, times(1)).findAllByUser(user, pageable);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_find_task_by_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            Long id = 1L;

            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
            when(taskRespository.findByIdAndUser(id, user)).thenReturn(Optional.of(task1));

            TaskDataDTO result = taskService.findById(id);

            assertNotNull(result);
            assertEquals(task1.getContent(), result.content());
            assertEquals(task1.getCompleted(), result.completed());

            verify(taskRespository).findByIdAndUser(id, user);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_task_by_invalid_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            Long id = 1000L;

            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
            when(taskRespository.findByIdAndUser(id, user)).thenReturn(Optional.empty());

            assertThrowsExactly(ResourceNotFoundException.class,
                    () -> taskService.findById(id));

            verify(taskRespository).findByIdAndUser(id, user);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_findAndReturnListTasks_byPartialContentFilter() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
            TaskFilterDTO filter = new TaskFilterDTO("Learn", null);

            when(taskRespository.findByFilters(filter.content(), filter.completed(), user))
                    .thenReturn(List.of(task1, task2));

            List<TaskDataDTO> result = taskService.findByFilter(filter);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(task ->
                    task.content().toLowerCase().contains(filter.content().toLowerCase())));

            verify(taskRespository).findByFilters(filter.content(), filter.completed(), user);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_findAndReturnListTasks_byPartialCompletedTrueFilter() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
            TaskFilterDTO filter = new TaskFilterDTO(null, Boolean.TRUE);

            when(taskRespository.findByFilters(filter.content(), filter.completed(), user))
                    .thenReturn(List.of(task2));

            List<TaskDataDTO> result = taskService.findByFilter(filter);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.stream().allMatch(task -> task.completed().equals(Boolean.TRUE)));
            assertTrue(result.stream().anyMatch(task -> task.content().equals(task2.getContent())));

            verify(taskRespository).findByFilters(filter.content(), filter.completed(), user);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_findAndReturnListTasks_byPartialCompletedFalseFilter() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            TaskFilterDTO filter = new TaskFilterDTO(null, Boolean.FALSE);

            when(taskRespository.findByFilters(filter.content(), filter.completed(), user))
                    .thenReturn(List.of(task1, task3));
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            List<TaskDataDTO> result = taskService.findByFilter(filter);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(task -> task.completed().equals(Boolean.FALSE)));
            assertTrue(result.stream().anyMatch(task -> task.content().equals(task1.getContent())));
            assertTrue(result.stream().anyMatch(task -> task.content().equals(task3.getContent())));

            verify(taskRespository).findByFilters(filter.content(), filter.completed(), user);
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_update_existing_task() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            when(taskRespository.findByIdAndUser(1L, user)).thenReturn(Optional.of(task1));
            when(taskRespository.save(any())).thenReturn(mock(Task.class));
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            TaskDTO taskUpdate = new TaskDTO("Migrate system to Spring boot", Boolean.FALSE);

            taskService.updateTask(1L, taskUpdate);

            assertEquals(taskUpdate.content(), task1.getContent());
            assertEquals(taskUpdate.completed(), task1.getCompleted());

            verify(taskRespository).findByIdAndUser(1L, user);
            verify(taskRespository).save(any(Task.class));
            mockedStatic.verify(AuthenticationUtil::getCurrentUser);
        }
    }

    @Test
    void should_throw_resourceNotFoundException_when_find_update_task_by_invalid_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            Long invalidId = 1L;

            when(taskRespository.findByIdAndUser(invalidId, user)).thenReturn(Optional.empty());
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            assertThrowsExactly(ResourceNotFoundException.class,
                    () -> taskService.updateTask(invalidId, new TaskDTO("some content", Boolean.FALSE)));

            verify(taskRespository).findByIdAndUser(invalidId, user);
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
        }
    }

    @Test
    void should_change_task_completedStatus() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            when(taskRespository.findByIdAndUser(1L, user)).thenReturn(Optional.of(task1));
            when(taskRespository.findByIdAndUser(2L, user)).thenReturn(Optional.of(task2));

            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            when(taskRespository.save(any(Task.class))).thenReturn(mock(Task.class), mock(Task.class));

            taskService.patchCompletedStatus(1L, new TaskCompletedDTO(Boolean.TRUE));
            taskService.patchCompletedStatus(2L, new TaskCompletedDTO(Boolean.FALSE));

            assertEquals(Boolean.TRUE, task1.getCompleted());
            assertEquals(Boolean.FALSE, task2.getCompleted());

            verify(taskRespository, times(1)).findByIdAndUser(1L, user);
            verify(taskRespository, times(1)).findByIdAndUser(2L, user);
            verify(taskRespository, times(2)).save(any(Task.class));
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
        }
    }

    @Test
    void should_throw_resourceNotFoundException_when_change_task_completedStatus_by_invalid_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            Long invalidId = 1000L;

            when(taskRespository.findByIdAndUser(invalidId, user)).thenReturn(Optional.empty());

            assertThrowsExactly(ResourceNotFoundException.class,
                    () -> taskService.patchCompletedStatus(invalidId, new TaskCompletedDTO(Boolean.FALSE)));

            verify(taskRespository).findByIdAndUser(invalidId, user);
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
        }
    }

    @Test
    void should_delete_task_by_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            when(taskRespository.findByIdAndUser(1L, user)).thenReturn(Optional.of(task1));
            doNothing().when(taskRespository).delete(task1);

            taskService.deleteTaskById(1L);

            verify(taskRespository).findByIdAndUser(1L, user);
            verify(taskRespository).delete(task1);
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
        }

    }

    @Test
    void should_throw_resourceNotFoundException_when_delete_task_completedStatus_by_nonExisting_id() {
        try (MockedStatic<AuthenticationUtil> mockedStatic = mockStatic(AuthenticationUtil.class)) {
            Long invalidId = 1000L;

            when(taskRespository.findByIdAndUser(invalidId, user)).thenReturn(Optional.empty());
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);

            assertThrowsExactly(ResourceNotFoundException.class,
                    () -> taskService.deleteTaskById(invalidId));

            verify(taskRespository).findByIdAndUser(invalidId, user);
            mockedStatic.when(AuthenticationUtil::getCurrentUser).thenReturn(user);
        }

    }

}