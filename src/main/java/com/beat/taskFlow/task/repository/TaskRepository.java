package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository
        extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssigneeId(Long assigneeId);
    
    List<Task> findByParentTaskId(Long parentTaskId);

    List<Task> findByProjectIdAndStatus(
            Long projectId,
            TaskStatus status
    );

    List<Task> findByProjectIdAndPriority(
            Long projectId,
            Priority priority
    );

    List<Task> findByProjectIdAndAssigneeId(
            Long projectId,
            Long assigneeId
    );

    List<Task> findByProjectIdAndDueDateBetween(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );

    Page<Task> findByProjectId(
            Long projectId,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndStatus(
            Long projectId,
            TaskStatus status,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndPriority(
            Long projectId,
            Priority priority,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndAssigneeId(
            Long projectId,
            Long assigneeId,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndDueDateBetween(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
}