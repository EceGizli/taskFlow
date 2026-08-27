package com.beat.taskFlow.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.beat.taskFlow.task.entity.concretes.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskIdOrderByIdAsc(Long taskId);
}