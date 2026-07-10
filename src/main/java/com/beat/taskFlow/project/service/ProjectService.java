package com.beat.taskFlow.project.service;

import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı! ID: " + id));
        return convertToResponse(project);
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        Project project = new Project(
                request.name(),
                request.description(),
                ProjectStatus.ACTIVE 
        );
        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }

    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Güncellenecek proje bulunamadı! ID: " + id));

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Silinecek proje bulunamadı! ID: " + id));
        projectRepository.delete(project);
    }

    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );
    }
}