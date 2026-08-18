package com.beat.taskFlow.project.repository;

import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
        SELECT DISTINCT p
        FROM Project p
        LEFT JOIN p.members m
        WHERE (p.owner.id = :userId OR m.id = :userId)
          AND (:status IS NULL OR p.status = :status)
          AND (:search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    List<Project> findAccessibleProjects(
            @Param("userId") Long userId,
            @Param("status") ProjectStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}