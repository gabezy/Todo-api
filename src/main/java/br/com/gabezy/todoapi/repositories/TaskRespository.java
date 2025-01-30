package br.com.gabezy.todoapi.repositories;

import br.com.gabezy.todoapi.domain.entity.Task;
import br.com.gabezy.todoapi.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRespository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndUser(Long id, User user);

    Page<Task> findAllByUser(User user, Pageable pageable);

    @Query("SELECT t FROM Task t " +
            "JOIN t.user u " +
            "WHERE (:content IS NULL OR LOWER(t.content) LIKE CONCAT('%', LOWER(:content), '%')) " +
            "AND (:completed IS NULL or t.completed = :completed) " +
            "AND t.user = :user"
    )
    List<Task> findByFilters(String content, Boolean completed, User user);
}
