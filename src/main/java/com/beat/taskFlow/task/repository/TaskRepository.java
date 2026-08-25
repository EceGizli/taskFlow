package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.user.entity.concretes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndIsDeletedFalse(Long id);

    List<Task> findByProject(Project project);

    List<Task> findByProjectAndIsDeletedFalse(Project project);

    List<Task> findByAssignee(User assignee);

    List<Task> findByAssigneeAndIsDeletedFalse(User assignee);

    List<Task> findByProjectAndAssignee(Project project, User assignee);

    List<Task> findByProjectAndAssigneeAndIsDeletedFalse(Project project, User assignee);

    List<Task> findByParentTask(Task parentTask);

    List<Task> findByParentTaskAndIsDeletedFalse(Task parentTask);

    List<Task> findByParentTaskIdAndIsDeletedFalse(Long parentTaskId);

    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.isDeleted = false " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Task> searchByProject(@Param("project") Project project, @Param("search") String search);
}