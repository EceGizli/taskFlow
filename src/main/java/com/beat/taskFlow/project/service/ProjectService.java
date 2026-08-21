package com.beat.taskFlow.project.service;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.dto.requests.AddMemberRequest;
import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.TransferOwnershipRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private void validateProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()));

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
    public List<ProjectResponse> getAllProjects(Authentication authentication, ProjectStatus status, String search, String sort) {
        User currentUser = getCurrentUser(authentication);

        Sort sorting = Sort.by(Sort.Direction.ASC, "createdAt");

        if ("createdAt,desc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);

        String searchValue = (search == null || search.isBlank()) ? "" : search;

        return projectRepository.findAccessibleProjects(
                currentUser.getId(),
                status,
                searchValue,
                pageable
        ).stream()
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
        project.setColor(request.color());
        project.setTag(request.tag());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(currentUser);

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
        project.setColor(request.color());
        project.setTag(request.tag());
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
    
    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectOwner(project, currentUser);

        User memberToAdd = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new NotFoundException("Eklenecek kullanıcı bulunamadı! ID: " + request.userId()));

        boolean isAlreadyMember = project.getMembers()
                .stream()
                .anyMatch(member -> member.getId().equals(memberToAdd.getId()));

        if (isAlreadyMember) {
            throw new AlreadyExistsException("Bu kullanıcı zaten projenin üyesidir.");
        }

        project.getMembers().add(memberToAdd);

        Project updatedProject = projectRepository.save(project);

        return convertToResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectOwner(project, currentUser);

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Proje sahibi projeden çıkarılamaz.");     
        }

        User memberToRemove = project.getMembers()
                .stream()
                .filter(member -> member.getId().equals(userId))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Kullanıcı bu projenin üyesi değildir."));

        project.getMembers().remove(memberToRemove);

        Project updatedProject = projectRepository.save(project);

        return convertToResponse(updatedProject);
    }
    
    @Transactional
    public void leaveProject(Long projectId, String currentUserEmail) {
        Project project = projectRepository.findById(projectId)
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
        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse transferOwnership(Long projectId, TransferOwnershipRequest req, String currentUserEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı: id=" + projectId));

        User currentOwner = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));

        if (!project.getOwner().getId().equals(currentOwner.getId())) {
            throw new AccessDeniedException("Yalnızca proje sahibi sahipliği devredebilir.");
        }

        User newOwner = userRepository.findById(req.newOwnerId())
                .orElseThrow(() -> new NotFoundException("Yeni sahip kullanıcı bulunamadı: id=" + req.newOwnerId()));

        if (newOwner.getId().equals(currentOwner.getId())) {
            throw new IllegalArgumentException("Zaten bu projenin sahibisiniz.");
        }

        project.getMembers().remove(newOwner);
        project.getMembers().add(currentOwner);
        project.setOwner(newOwner);

        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }

    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getColor(),
                project.getTag(),
                project.getStatus(),
                project.getOwner().getId(),
                project.getOwner().getName(),
                project.getMembers().size() + 1, // owner dahil
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}