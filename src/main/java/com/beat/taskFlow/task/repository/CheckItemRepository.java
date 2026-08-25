package com.beat.taskFlow.task.repository;

import com.beat.taskFlow.task.entity.concretes.CheckItem;
import com.beat.taskFlow.task.entity.concretes.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckItemRepository extends JpaRepository<CheckItem, Long> {

    List<CheckItem> findByTaskOrderByIdAsc(Task task);

    List<CheckItem> findByTaskIdOrderByIdAsc(Long taskId);
}