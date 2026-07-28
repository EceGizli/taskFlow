package com.beat.taskFlow.project.service;

import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private void validateProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu projeye erişim yetkiniz bulunmamaktadır!");
        }
    }

    private void validateProjectOwner(Project project, User user) {
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bu işlem için proje sahibi olmanız gerekmektedir!");
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return projectRepository.findAccessibleProjects(currentUser.getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + id));

        validateProjectAccess(project, currentUser);
        return convertToResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(currentUser);
        project.getMembers().add(currentUser);

        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Güncellenecek proje bulunamadı! ID: " + id));

        validateProjectOwner(project, currentUser);

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silinecek proje bulunamadı! ID: " + id));

        validateProjectOwner(project, currentUser);
        projectRepository.delete(project);
    }

    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOwner().getId(),
                project.getOwner().getName(),
                project.getMembers().size(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}