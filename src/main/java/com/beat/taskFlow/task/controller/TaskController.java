package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.*;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.dto.responses.TaskStatusHistoryResponse;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "Görev yönetimi uçları")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Görev oluştur",
            description = "Bir projeye yeni görev oluşturur."
    )
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, request, authentication));
    }

    @Operation(
            summary = "Proje görevlerini listele",
            description = "Bir projenin görevlerini filtreleme, sıralama ve sayfalama seçenekleriyle listeler."
    )
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) Long labelId,
            @RequestParam(required = false) String search,
            Pageable pageable,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getTasksByProjectId(
                projectId, status, priority, assigneeId, dueDate, labelId, search, pageable, authentication
        ));
    }

    @Operation(
            summary = "Görevi getir",
            description = "Belirtilen ID değerine sahip görevi getirir."
    )
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getTaskById(id, authentication));
    }

    @Operation(
            summary = "Görevi güncelle",
            description = "Belirtilen görevin bilgilerini günceller."
    )
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.updateTask(id, request, authentication));
    }

    @Operation(
            summary = "Görev durumunu güncelle",
            description = "Belirtilen görevin durumunu günceller."
    )
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.updateTaskStatus(id, request, authentication));
    }

    @Operation(
            summary = "Görev durumlarını toplu güncelle",
            description = "Birden fazla görevin durumunu tek istekte günceller."
    )
    @PatchMapping("/tasks/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateStatusRequest request,
            Authentication authentication) {

        taskService.bulkUpdateStatus(request, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Göreve kullanıcı ata",
            description = "Belirtilen göreve bir kullanıcı atar."
    )
    @PatchMapping("/tasks/{id}/assignee")
    public ResponseEntity<TaskResponse> updateTaskAssignee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskAssigneeRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.updateTaskAssignee(id, request, authentication));
    }

    @Operation(
            summary = "Görevi sil",
            description = "Belirtilen görevi siler."
    )
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        taskService.deleteTask(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Bana atanan görevleri listele",
            description = "Giriş yapan kullanıcıya atanmış görevleri listeler."
    )
    @GetMapping("/tasks/assigned-to-me")
    public ResponseEntity<List<TaskResponse>> getTasksAssignedToMe(
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getTasksAssignedToMe(authentication));
    }

    @Operation(
            summary = "Görev durum geçmişini getir",
            description = "Belirtilen görevin durum değişiklik geçmişini getirir."
    )
    @GetMapping("/tasks/{id}/status-history")
    public ResponseEntity<List<TaskStatusHistoryResponse>> getTaskStatusHistory(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getTaskStatusHistory(id, authentication));
    }

    @Operation(
            summary = "Göreve etiket ekle",
            description = "Belirtilen etiketi göreve ekler."
    )
    @PostMapping("/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskResponse> addLabelToTask(
            @PathVariable Long id,
            @PathVariable Long labelId,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.addLabelToTask(id, labelId, authentication)
        );
    }

    @Operation(
            summary = "Görevden etiket kaldır",
            description = "Belirtilen etiketi görevden kaldırır."
    )
    @DeleteMapping("/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskResponse> removeLabelFromTask(
            @PathVariable Long id,
            @PathVariable Long labelId,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.removeLabelFromTask(id, labelId, authentication)
        );
    }

    @Operation(
            summary = "Görevin alt görevlerini listele",
            description = "Belirtilen görevin alt görevlerini (subtask) listeler."
    )
    @GetMapping("/tasks/{id}/subtasks")
    public ResponseEntity<List<TaskResponse>> getSubtasks(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getSubtasks(id, authentication));
    }

    @Operation(
            summary = "Süresi geçmiş görevleri listele",
            description = "Bir projede son teslim tarihi geçmiş ve DONE durumunda olmayan görevleri listeler."
    )
    @GetMapping("/projects/{projectId}/tasks/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks(
            @PathVariable Long projectId,
            Authentication authentication) {

        return ResponseEntity.ok(taskService.getOverdueTasks(projectId, authentication));
    }
}