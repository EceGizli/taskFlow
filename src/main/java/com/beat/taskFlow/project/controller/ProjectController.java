package com.beat.taskFlow.project.controller;

import com.beat.taskFlow.project.dto.requests.AddMemberRequest;
import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.dto.responses.ProjectStatsResponse;
import com.beat.taskFlow.project.service.ProjectService;
import com.beat.taskFlow.task.service.TaskService;

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
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(Authentication authentication) {
        return ResponseEntity.ok(projectService.getAllProjects(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.getProjectById(id, authentication));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateProjectRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProject(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, Authentication authentication) {
        projectService.deleteProject(id, authentication);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.addMember(id, request, authentication)
        );
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.removeMember(id, userId, authentication)
        );
    }
    
    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.getProjectStats(id, authentication)
        );
    }
}