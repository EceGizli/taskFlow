package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.BulkUpdateStatusRequest;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody CreateTaskRequest request) {

        return taskService.createTask(projectId, request);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> getTasksByProjectId(
            @PathVariable Long projectId) {

        return taskService.getTasksByProjectId(projectId);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse getTaskById(
            @PathVariable Long id) {

        return taskService.getTaskById(id);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return taskService.updateTask(id, request);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskResponse updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        return taskService.updateTaskStatus(id, request);
    }

    @PatchMapping("/tasks/bulk-status")
    public List<TaskResponse> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateStatusRequest request) {
        
        return taskService.bulkUpdateStatus(request);
    }

    @PostMapping("/tasks/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse duplicateTask(
            @PathVariable Long id) {
        
        return taskService.duplicateTask(id);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);
    }
}