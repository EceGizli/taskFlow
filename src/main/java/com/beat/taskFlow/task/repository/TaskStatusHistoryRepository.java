package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.concretes.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {
    List<TaskStatusHistory> findByTaskOrderByCreatedAtDesc(Task task);
}