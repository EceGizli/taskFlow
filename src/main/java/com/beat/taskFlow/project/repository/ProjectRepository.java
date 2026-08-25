package com.beat.taskFlow.project.repository;

import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.user.entity.concretes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
           "WHERE (p.owner = :user OR m = :user) AND p.isDeleted = false")
    List<Project> findAccessibleProjects(@Param("user") User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
           "WHERE (p.owner = :user OR m = :user) " +
           "AND p.isDeleted = false " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Project> searchAccessibleProjects(@Param("user") User user, @Param("search") String search);
}