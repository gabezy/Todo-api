package br.com.gabezy.todoapi.repositories;

import br.com.gabezy.todoapi.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRespository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t " +
            "WHERE (:content IS NULL OR LOWER(t.content) LIKE CONCAT('%', LOWER(:content), '%')) " +
            "AND (:compelted IS NULL OR t.completed = :completed)"
    )
    List<Task> findTaskByContentContainingAndCompleted(String content, Boolean completed);

}
