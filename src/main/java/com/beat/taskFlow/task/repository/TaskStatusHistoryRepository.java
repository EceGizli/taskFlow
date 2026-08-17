package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.task.entity.concretes.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskStatusHistoryRepository
        extends JpaRepository<TaskStatusHistory, Long> {

    List<TaskStatusHistory> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}