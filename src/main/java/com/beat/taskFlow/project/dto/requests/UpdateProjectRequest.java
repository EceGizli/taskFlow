package com.beat.taskFlow.project.dto.requests;

import com.beat.taskFlow.project.entity.enums.ProjectStatus;

public record UpdateProjectRequest(
    String name,
    String description,
    ProjectStatus status
) {}