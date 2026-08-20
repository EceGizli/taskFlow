package com.beat.taskFlow.project.controller;

import com.beat.taskFlow.project.dto.requests.AddMemberRequest;
import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.TransferOwnershipRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.dto.responses.ProjectStatsResponse;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.service.ProjectService;
import com.beat.taskFlow.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Proje yönetimi uçları")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @Operation(summary = "Projeleri listele", description = "Giriş yapan kullanıcının sahibi veya üyesi olduğu projeleri, opsiyonel filtre/arama/sıralama ile listeler.")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Authentication authentication) {

        return ResponseEntity.ok(projectService.getAllProjects(authentication, status, search, sort));
    }

    @Operation(summary = "Proje detayını getir", description = "Belirtilen ID'ye sahip projenin detaylarını getirir.")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.getProjectById(id, authentication));
    }

    @Operation(summary = "Proje oluştur", description = "Yeni bir proje oluşturur. Oluşturan kullanıcı otomatik olarak proje sahibi olur.")
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, authentication));
    }

    @Operation(summary = "Projeyi güncelle", description = "Belirtilen projenin bilgilerini günceller (Sadece proje sahibi).")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProject(id, request, authentication));
    }

    @Operation(summary = "Projeyi sil", description = "Belirtilen projeyi ve bağlı tüm kaynakları siler (Sadece proje sahibi).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            Authentication authentication) {
        projectService.deleteProject(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Projeye üye ekle", description = "Belirtilen projeye yeni bir üye ekler.")
    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.addMember(id, request, authentication)
        );
    }

    @Operation(summary = "Projeden üye çıkar", description = "Belirtilen üyeyi projeden çıkarır.")
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.removeMember(id, userId, authentication)
        );
    }

    @Operation(summary = "Proje istatistiklerini getir", description = "Projedeki görevlerin durum dağılım istatistiklerini getirir.")
    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.getProjectStats(id, authentication)
        );
    }

    @Operation(summary = "Projeden ayrıl", description = "Giriş yapan kullanıcı, sahibi olmadığı bir projeden kendisini üyelikten çıkarır.")
    @DeleteMapping("/{id}/members/leave")
    public ResponseEntity<Void> leaveProject(
            @PathVariable Long id,
            Authentication authentication) {
        projectService.leaveProject(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Proje sahipliğini devret", description = "Projenin sahipliğini başka bir üyeye devreder.")
    @PutMapping("/{id}/transfer-ownership")
    public ResponseEntity<ProjectResponse> transferOwnership(
            @PathVariable Long id,
            @Valid @RequestBody TransferOwnershipRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.transferOwnership(id, request, authentication.getName()));
    }
}