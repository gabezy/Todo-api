package br.com.gabezy.todoapi.services;

import br.com.gabezy.todoapi.domain.dto.TaskCompletedDTO;
import br.com.gabezy.todoapi.domain.dto.TaskDTO;
import br.com.gabezy.todoapi.domain.dto.TaskFilterDTO;
import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.TaskRespository;
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
        return repository.save(task);
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND));
    }

    public List<Task> findByFilter(TaskFilterDTO dto) {
        return repository.findByContentOrCompleted(dto.content(), dto.completed());
    }

    public void updateTask(Long taskId, TaskDTO dto) {
        Task taskUpdate = this.findById(taskId);
        taskUpdate.setContent(dto.content());
        taskUpdate.setCompleted(dto.completed());
        repository.save(taskUpdate);
    }

    public void patchCompletedStatus(Long taskId, TaskCompletedDTO dto) {
        Task task = this.findById(taskId);
        task.setCompleted(dto.completed());
        repository.save(task);
    }

    public void deleteTaskById(Long taskId) {
        Task taskDelete = this.findById(taskId);
        repository.delete(taskDelete);
    }
}
