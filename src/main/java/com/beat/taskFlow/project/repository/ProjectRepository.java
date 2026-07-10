package com.beat.taskFlow.project.repository;

import com.beat.taskFlow.project.entity.concretes.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}