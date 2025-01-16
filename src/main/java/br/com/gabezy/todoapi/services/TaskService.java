package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDataDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.TaskRespository;
import br.com.gabezy.todoapi.utils.AuthenticationUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRespository repository;

    public TaskService(TaskRespository taskRespository) {
        this.repository = taskRespository;
    }

    public Task createTask(TaskDTO newTask) {
        Task task = new Task();
        task.setContent(newTask.content());
        task.setCompleted(newTask.completed());
        task.setUser(AuthenticationUtil.getCurrentUser());
        return repository.save(task);
    }

    public List<TaskDataDTO> findAll() {
        User currentUser = AuthenticationUtil.getCurrentUser();
        return repository.findAllByUser(currentUser)
                .stream()
                .map(this::mapToDataDTO)
                .toList();
    }

    public TaskDataDTO findById(Long id) {
        var task = this.findTaskById(id);
        return mapToDataDTO(task);
    }

    public List<TaskDataDTO> findByFilter(TaskFilterDTO dto) {
        User currentUser = AuthenticationUtil.getCurrentUser();
        return repository.findByFilters(dto.content(), dto.completed(), currentUser)
                .stream()
                .map(this::mapToDataDTO)
                .toList();
    }

    public void updateTask(Long taskId, TaskDTO dto) {
        Task taskUpdate = this.findTaskById(taskId);
        taskUpdate.setContent(dto.content());
        taskUpdate.setCompleted(dto.completed());
        repository.save(taskUpdate);
    }

    public void patchCompletedStatus(Long taskId, TaskCompletedDTO dto) {
        Task task = this.findTaskById(taskId);
        task.setCompleted(dto.completed());
        repository.save(task);
    }

    public void deleteTaskById(Long taskId) {
        Task taskDelete = this.findTaskById(taskId);
        repository.delete(taskDelete);
    }

    private Task findTaskById(Long id) {
        User currentUser = AuthenticationUtil.getCurrentUser();
        return repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND));
    }

    private TaskDataDTO mapToDataDTO(Task task) {
        return new TaskDataDTO(task.getId(), task.getContent(), task.getCompleted());
    }
}
