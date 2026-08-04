package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.BulkUpdateStatusRequest;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskAssigneeRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        return taskService.createTask(projectId, request, authentication);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> getTasksByProjectId(
            @PathVariable Long projectId,
            Authentication authentication) {

        return taskService.getTasksByProjectId(projectId, authentication);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse getTaskById(
            @PathVariable Long id,
            Authentication authentication) {

        return taskService.getTaskById(id, authentication);
    }

    @GetMapping("/tasks/assigned-to-me")
    public List<TaskResponse> getAssignedTasks(Authentication authentication) {

        return taskService.getAssignedTasks(authentication);
    }
    
    @PutMapping("/tasks/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {

        return taskService.updateTask(id, request, authentication);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskResponse updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {

        return taskService.updateTaskStatus(id, request, authentication);
    }
    
    @PatchMapping("/tasks/{id}/assignee")
    public TaskResponse assignTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskAssigneeRequest request,
            Authentication authentication) {

        return taskService.assignTask(id, request, authentication);
    }

    @PatchMapping("/tasks/bulk-status")
    public List<TaskResponse> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateStatusRequest request,
            Authentication authentication) {

        return taskService.bulkUpdateStatus(request, authentication);
    }

    @PostMapping("/tasks/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse duplicateTask(
            @PathVariable Long id,
            Authentication authentication) {

        return taskService.duplicateTask(id, authentication);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        taskService.deleteTask(id, authentication);
    }
}