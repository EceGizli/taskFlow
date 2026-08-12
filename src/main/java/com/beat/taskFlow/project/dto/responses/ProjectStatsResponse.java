package com.beat.taskFlow.project.dto.responses;

public record ProjectStatsResponse(
        long totalTasks,
        long todo,
        long inProgress,
        long done
) {}