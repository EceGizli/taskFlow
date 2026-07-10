package com.beat.taskFlow.project.entity.concretes;

import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    public Project() {
    }

    public Project(String name, String description, ProjectStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}