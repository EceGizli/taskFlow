package com.beat.taskFlow.project.dto.requests;

public record CreateProjectRequest(
    String name,
    String description
) {}