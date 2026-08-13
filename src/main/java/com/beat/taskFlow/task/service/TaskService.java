package com.beat.taskFlow.task.service;

import com.beat.taskFlow.common.exception.InvalidTaskStatusException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.dto.responses.ProjectStatsResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.dto.requests.BulkUpdateStatusRequest;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskAssigneeRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private void validateTaskAccess(Task task, User user) {
        Project project = task.getProject();

        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu göreve erişim yetkiniz bulunmamaktadır.");
        }
    }

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı. id = " + projectId));

        boolean hasAccess = project.getOwner().getId().equals(currentUser.getId()) ||
                project.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new AccessDeniedException("Bu projeye görev ekleme yetkiniz bulunmamaktadır.");
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .estimatedHours(request.estimatedHours())
                .project(project)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProjectId(
            Long projectId,
            String search,
            TaskStatus status,
            Priority priority,
            Long assigneeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Proje bulunamadı. id = " + projectId
                        ));

        boolean hasAccess =
                project.getOwner().getId().equals(currentUser.getId()) ||
                project.getMembers().stream()
                        .anyMatch(member ->
                                member.getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "Bu projeye erişim yetkiniz bulunmamaktadır."
            );
        }

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Başlangıç tarihi bitiş tarihinden sonra olamaz."
            );
        }


        Specification<Task> specification =
        		(root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);

        			if (search != null && !search.trim().isEmpty()) {

        				String searchPattern = "%" + search.trim().toLowerCase() + "%";

        					specification = specification.and( (root, query, cb) ->
        						cb.or( cb.like( cb.lower(root.get("title")), searchPattern ),
		 						cb.like(cb.lower(root.get("description")), searchPattern)));
                        }                        
                 
        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("status"), status)
            );
        }

        if (priority != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("priority"), priority)
            );
        }

        if (assigneeId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("assignee").get("id"), assigneeId)
            );
        }

        if (startDate != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("dueDate"),
                                    startDate
                            )
            );
        }

        if (endDate != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("dueDate"),
                                    endDate
                            )
            );
        }

        Page<Task> taskPage =
                taskRepository.findAll(specification, pageable);

        return taskPage.map(this::mapToResponse);
     }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        validateTaskAccess(task, currentUser);

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        validateTaskAccess(task, currentUser);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setEstimatedHours(request.estimatedHours());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        validateTaskAccess(task, currentUser);

        validateStatusTransition(task.getStatus(), request.status());

        task.setStatus(request.status());
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public List<TaskResponse> bulkUpdateStatus(BulkUpdateStatusRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Task> tasksToUpdate = new ArrayList<>();

        for (Long id : request.taskIds()) {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Toplu güncelleme sırasında görev bulunamadı. id = " + id));

            validateTaskAccess(task, currentUser);

            validateStatusTransition(task.getStatus(), request.status());
            task.setStatus(request.status());
            tasksToUpdate.add(task);
        }

        List<Task> updatedTasks = taskRepository.saveAll(tasksToUpdate);

        return updatedTasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TaskResponse duplicateTask(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task sourceTask = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kopyalanacak görev bulunamadı. id = " + id));

        validateTaskAccess(sourceTask, currentUser);

        Task duplicatedTask = Task.builder()
                .title(sourceTask.getTitle() + " - Kopya")
                .description(sourceTask.getDescription())
                .status(TaskStatus.TODO)
                .priority(sourceTask.getPriority())
                .dueDate(sourceTask.getDueDate())
                .estimatedHours(sourceTask.getEstimatedHours())
                .project(sourceTask.getProject())
                .build();

        Task savedDuplicate = taskRepository.save(duplicatedTask);
        return mapToResponse(savedDuplicate);
    }

    @Transactional
    public void deleteTask(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        validateTaskAccess(task, currentUser);

        taskRepository.delete(task);
    }
    
    @Transactional
    public TaskResponse assignTask(Long id, UpdateTaskAssigneeRequest request, Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Görev bulunamadı. id = " + id));

        validateTaskAccess(task, currentUser);

        User assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() ->
                        new NotFoundException("Kullanıcı bulunamadı. id = " + request.assigneeId()));

        Project project = task.getProject();

        boolean isProjectMember =
                project.getOwner().getId().equals(assignee.getId()) ||
                project.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(assignee.getId()));

        if (!isProjectMember) {
            throw new AccessDeniedException(
                    "Atanacak kullanıcı bu projenin üyesi değildir.");
        }

        task.setAssignee(assignee);

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getAssignedTasks(Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return taskRepository.findByAssigneeId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(
            Long projectId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Proje bulunamadı. id = " + projectId
                        ));

        boolean hasAccess =
                project.getOwner().getId().equals(currentUser.getId()) ||
                project.getMembers().stream()
                        .anyMatch(member ->
                                member.getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "Bu projeye erişim yetkiniz bulunmamaktadır."
            );
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        long totalTasks = tasks.size();

        long todo = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.TODO)
                .count();

        long inProgress = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        long done = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        return new ProjectStatsResponse(
                totalTasks,
                todo,
                inProgress,
                done
        );
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(
            Long projectId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Proje bulunamadı. id = " + projectId
                        ));

        boolean hasAccess =
                project.getOwner().getId().equals(currentUser.getId()) ||
                project.getMembers().stream()
                        .anyMatch(member ->
                                member.getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "Bu projeye erişim yetkiniz bulunmamaktadır."
            );
        }

        LocalDate today = LocalDate.now();

        List<Task> overdueTasks = taskRepository.findByProjectId(projectId)
                .stream()
                .filter(task ->
                        task.getDueDate() != null &&
                        task.getDueDate().isBefore(today) &&
                        task.getStatus() != TaskStatus.DONE
                )
                .toList();

        return overdueTasks.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == TaskStatus.DONE && newStatus != TaskStatus.DONE) {
            throw new InvalidTaskStatusException("Tamamlanan görev tekrar başka bir duruma geçirilemez.");
        }

        if (currentStatus == TaskStatus.TODO && newStatus == TaskStatus.DONE) {
            throw new InvalidTaskStatusException("Görev tamamlanmadan önce IN_PROGRESS durumuna geçirilmelidir.");
        }
    }

    private TaskResponse mapToResponse(Task task) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getEstimatedHours(),
                task.getCreatedAt(),
                task.getUpdatedAt(),

                task.getAssignee() != null
                        ? task.getAssignee().getId()
                        : null,

                task.getAssignee() != null
                        ? task.getAssignee().getName()
                        : null
        );
    }
}