package com.beat.taskFlow.project.service;

import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.dto.requests.AddMemberRequest;
import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.TransferOwnershipRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.dto.responses.ProjectStatsResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Authentication authentication) {
        User owner = getCurrentUser(authentication);

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .status(ProjectStatus.ACTIVE)
                .color(request.color())
                .tag(request.tag())
                .owner(owner)
                .build();

        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Authentication authentication, ProjectStatus status, String tag, String search) {
        User currentUser = getCurrentUser(authentication);
        List<Project> projects;

        if (search != null && !search.trim().isEmpty()) {
            projects = projectRepository.searchAccessibleProjects(currentUser, search.trim());
        } else {
            projects = projectRepository.findAccessibleProjects(currentUser);
        }

        return projects.stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> tag == null || (p.getTag() != null && p.getTag().equalsIgnoreCase(tag)))
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + id));

        validateProjectAccess(project, currentUser);
        return convertToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + id));

        validateProjectOwner(project, currentUser);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            project.setName(request.name().trim());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.color() != null) {
            project.setColor(request.color());
        }
        if (request.tag() != null) {
            project.setTag(request.tag());
        }

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + id));

        validateProjectOwner(project, currentUser);

        project.setDeleted(true);
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectOwner(project, currentUser);

        User newMember = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı! ID: " + request.userId()));

        if (project.getOwner().getId().equals(newMember.getId())) {
            throw new IllegalArgumentException("Proje sahibi zaten projenin doğal üyesidir.");
        }

        project.getMembers().add(newMember);
        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectOwner(project, currentUser);

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Proje sahibi projeden çıkarılamaz.");
        }

        User memberToRemove = project.getMembers()
                .stream()
                .filter(member -> member.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Kullanıcı bu projenin üyesi değildir."));

        project.getMembers().remove(memberToRemove);

        List<Task> assignedTasks = taskRepository.findByProjectAndAssignee(project, memberToRemove);
        assignedTasks.forEach(task -> task.setAssignee(null));
        taskRepository.saveAll(assignedTasks);

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    @Transactional
    public void leaveProject(Long projectId, String currentUserEmail) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı: id=" + projectId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));

        if (project.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Proje sahibi projeden ayrılamaz. Önce sahipliği devretmelisiniz.");
        }

        if (!project.getMembers().contains(currentUser)) {
            throw new NotFoundException("Bu projede üye değilsiniz.");
        }

        project.getMembers().remove(currentUser);

        List<Task> assignedTasks = taskRepository.findByProjectAndAssignee(project, currentUser);
        assignedTasks.forEach(task -> task.setAssignee(null));
        taskRepository.saveAll(assignedTasks);

        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse transferOwnership(Long projectId, TransferOwnershipRequest request, String currentUserEmail) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + currentUserEmail));

        validateProjectOwner(project, currentUser);

        User newOwner = userRepository.findById(request.newOwnerId())
                .orElseThrow(() -> new NotFoundException("Yeni sahip kullanıcı bulunamadı! ID: " + request.newOwnerId()));

        if (project.getOwner().getId().equals(newOwner.getId())) {
            throw new IllegalArgumentException("Kullanıcı zaten bu projenin sahibidir.");
        }

        project.getMembers().remove(newOwner);
        project.getMembers().add(currentUser);
        project.setOwner(newOwner);

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(Long projectId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectAccess(project, currentUser);

        List<Task> tasks = taskRepository.findByProjectAndIsDeletedFalse(project);

        long totalTasks = tasks.size();
        long todoCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        return new ProjectStatsResponse(totalTasks, todoCount, inProgressCount, doneCount);
    }

    public void validateProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu projeye erişim yetkiniz yok!");
        }
    }

    public void validateProjectOwner(Project project, User user) {
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bu işlem için proje sahibi olmanız gerekmektedir!");
        }
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Giriş yapan kullanıcı bulunamadı!"));
    }

    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getColor(),
                project.getTag(),
                project.getOwner().getId(),
                project.getOwner().getName(),
                project.getMembers() != null ? project.getMembers().size() : 0,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}