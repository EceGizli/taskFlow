package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
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

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);
    }
}