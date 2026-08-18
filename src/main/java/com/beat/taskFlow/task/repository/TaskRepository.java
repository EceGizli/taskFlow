package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.user.entity.concretes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignee(User assignee);
}