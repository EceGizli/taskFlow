package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.*;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.dto.responses.TaskStatusHistoryResponse;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request, authentication));
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskResponse>> getTasksByProjectId(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) Long labelId,
            Pageable pageable,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId, status, priority, assigneeId, dueDate, labelId, pageable, authentication));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.getTaskById(id, authentication));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(id, request, authentication));
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request, authentication));
    }

    @PatchMapping("/tasks/bulk-status")
    public ResponseEntity<List<TaskResponse>> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.bulkUpdateStatus(request, authentication));
    }

    @PatchMapping("/tasks/{id}/assignee")
    public ResponseEntity<TaskResponse> updateTaskAssignee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskAssigneeRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTaskAssignee(id, request, authentication));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {
        taskService.deleteTask(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/assigned-to-me")
    public ResponseEntity<List<TaskResponse>> getTasksAssignedToMe(Authentication authentication) {
        return ResponseEntity.ok(taskService.getTasksAssignedToMe(authentication));
    }

    @GetMapping("/tasks/{id}/status-history")
    public ResponseEntity<List<TaskStatusHistoryResponse>> getTaskStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskStatusHistory(id));
    }

    @PostMapping("/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskResponse> addLabelToTask(
            @PathVariable Long id,
            @PathVariable Long labelId,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.addLabelToTask(id, labelId, authentication));
    }

    @DeleteMapping("/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskResponse> removeLabelFromTask(
            @PathVariable Long id,
            @PathVariable Long labelId,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.removeLabelFromTask(id, labelId, authentication));
    }
}