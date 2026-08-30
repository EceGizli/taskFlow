package com.beat.taskFlow.task.service;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.InvalidTaskStatusException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.label.dto.responses.LabelResponse;
import com.beat.taskFlow.label.entity.Label;
import com.beat.taskFlow.label.repository.LabelRepository;
import com.beat.taskFlow.notification.service.NotificationService;
import com.beat.taskFlow.project.dto.responses.ProjectStatsResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.concretes.ProjectMember;
import com.beat.taskFlow.project.entity.enums.ProjectRole;
import com.beat.taskFlow.project.repository.ProjectMemberRepository;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.dto.requests.*;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.dto.responses.TaskStatusHistoryResponse;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.concretes.TaskStatusHistory;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.CheckItemRepository;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.repository.TaskStatusHistoryRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beat.taskFlow.task.entity.concretes.CheckItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final LabelRepository labelRepository;
    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;
    private final CheckItemRepository checkItemRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private void validateProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu projeye erişim yetkiniz bulunmamaktadır!");
        }
    }

    public void validateTaskModificationAccess(Project project, User user) {
        if (project.getOwner() != null && project.getOwner().getId() != null && user != null && user.getId() != null) {
            if (project.getOwner().getId().equals(user.getId())) {
                return; 
            }
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bu projeye erişim yetkiniz bulunmamaktadır."));

        if (member.getRole() == ProjectRole.VIEWER) {
            throw new AccessDeniedException("VIEWER rolündeki üyeler görevlerde değişiklik yapamaz.");
        }
    }

    private void validateTaskAccess(Task task, User user) {
        validateProjectAccess(task.getProject(), user);
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == newStatus) return;

        boolean allowed = switch (currentStatus) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TaskStatus.DONE || newStatus == TaskStatus.TODO;
            case DONE -> newStatus == TaskStatus.IN_PROGRESS;
        };

        if (!allowed) {
            throw new InvalidTaskStatusException(
                    "Geçersiz durum geçişi: " + currentStatus + " -> " + newStatus
            );
        }
    }
    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateTaskModificationAccess(project, currentUser);
        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new NotFoundException("Atanan kullanıcı bulunamadı! ID: " + request.assigneeId()));

            boolean isAssigneeMember = project.getMembers().stream()
                    .anyMatch(m -> m.getId().equals(request.assigneeId())) || project.getOwner().getId().equals(request.assigneeId());

            if (!isAssigneeMember) {
                throw new AccessDeniedException("Yalnızca proje üyelerine görev atanabilir!");
            }
        }

        Task parentTask = null;
        if (request.parentTaskId() != null) {
            parentTask = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new NotFoundException("Üst görev bulunamadı! ID: " + request.parentTaskId()));

            if (!parentTask.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException(
                        "Üst görev, görevin oluşturulacağı projeyle aynı projeye ait değil. Üst görev projesi: "
                                + parentTask.getProject().getId() + ", hedef proje: " + projectId
                );
            }
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : Priority.MEDIUM)
                .dueDate(request.dueDate())
                .estimatedHours(request.estimatedHours())
                .project(project)
                .assignee(assignee)
                .parentTask(parentTask)
                .build();

        Task savedTask = taskRepository.save(task);

        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(savedTask)
                .status(savedTask.getStatus())
                .build();
        taskStatusHistoryRepository.save(history);

        return mapToResponse(savedTask);
    }
    
    @Transactional
    public TaskResponse duplicateTask(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task originalTask = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskModificationAccess(originalTask.getProject(), currentUser);

        Task duplicatedTask = Task.builder()
                .title(originalTask.getTitle() + " (Kopya)")
                .description(originalTask.getDescription())
                .status(TaskStatus.TODO)
                .priority(originalTask.getPriority())
                .dueDate(originalTask.getDueDate())
                .estimatedHours(originalTask.getEstimatedHours())
                .project(originalTask.getProject())
                .assignee(originalTask.getAssignee())
                .parentTask(null)
                .build();

        if (originalTask.getLabels() != null) {
            duplicatedTask.getLabels().addAll(originalTask.getLabels());
        }

        Task savedTask = taskRepository.save(duplicatedTask);
        
        List<CheckItem> originalCheckItems =
                checkItemRepository.findByTaskOrderByIdAsc(originalTask);

        List<CheckItem> duplicatedCheckItems = originalCheckItems.stream()
                .map(checkItem -> CheckItem.builder()
                        .title(checkItem.getTitle())
                        .isCompleted(false)
                        .task(savedTask)
                        .build())
                .toList();

        checkItemRepository.saveAll(duplicatedCheckItems);

        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(savedTask)
                .status(savedTask.getStatus())
                .build();

        taskStatusHistoryRepository.save(history);

        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProjectId(
            Long projectId,
            TaskStatus status,
            Priority priority,
            Long assigneeId,
            LocalDate dueDate,
            Long labelId,
            String search,
            Pageable pageable,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectAccess(project, currentUser);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("project").get("id"), projectId));
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (dueDate != null) {
                predicates.add(cb.equal(root.get("dueDate"), dueDate));
            }
            if (labelId != null) {
                Join<Task, Label> labelJoin = root.join("labels");
                predicates.add(cb.equal(labelJoin.get("id"), labelId));
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
        		.orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskAccess(task, currentUser);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskModificationAccess(task.getProject(), currentUser);
        
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
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskModificationAccess(task.getProject(), currentUser);
        
        if (task.getStatus() == request.status()) {
            return mapToResponse(task);
        }

        validateStatusTransition(task.getStatus(), request.status());
        task.setStatus(request.status());

        Task updatedTask = taskRepository.save(task);

        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(updatedTask)
                .status(updatedTask.getStatus())
                .build();
        taskStatusHistoryRepository.save(history);

        return mapToResponse(updatedTask);
    }

    @Transactional
    public List<TaskResponse> bulkUpdateStatus(BulkUpdateStatusRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Task> tasksToUpdate = new ArrayList<>();
        List<TaskStatusHistory> histories = new ArrayList<>();

        for (Long id : request.taskIds()) {
        	Task task = taskRepository.findByIdAndIsDeletedFalse(id)
        	        .orElseThrow(() -> new NotFoundException("Toplu güncelleme sırasında görev bulunamadı. id = " + id));

            validateTaskModificationAccess(task.getProject(), currentUser);
            validateStatusTransition(task.getStatus(), request.status());

            if (task.getStatus() == request.status()) {
                continue;
            }

            task.setStatus(request.status());
            tasksToUpdate.add(task);
        }

        List<Task> updatedTasks = taskRepository.saveAll(tasksToUpdate);

        for (Task task : updatedTasks) {
            TaskStatusHistory history = TaskStatusHistory.builder()
                    .task(task)
                    .status(task.getStatus())
                    .build();
            histories.add(history);
        }

        taskStatusHistoryRepository.saveAll(histories);

        return updatedTasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TaskResponse updateTaskAssignee(Long id, UpdateTaskAssigneeRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskModificationAccess(task.getProject(), currentUser);
        
        if (request.assigneeId() == null) {
            task.setAssignee(null);
        } else {
            User newAssignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı! ID: " + request.assigneeId()));

            boolean isAssigneeMember = task.getProject().getMembers().stream()
                    .anyMatch(m -> m.getId().equals(request.assigneeId())) || task.getProject().getOwner().getId().equals(request.assigneeId());

            if (!isAssigneeMember) {
                throw new AccessDeniedException("Yalnızca proje üyelerine görev atanabilir!");
            }

            task.setAssignee(newAssignee);
        }

        Task updatedTask = taskRepository.save(task);

        if (updatedTask.getAssignee() != null && !updatedTask.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                    updatedTask.getAssignee(),
                    "Yeni Görev Atandı",
                    "\"" + updatedTask.getTitle() + "\" başlıklı görev size atandı."
            );
        }

        return mapToResponse(updatedTask);
    }
    
    @Transactional
    public void deleteTask(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + id));

        validateTaskModificationAccess(task.getProject(), currentUser);
        
        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksAssignedToMe(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return taskRepository.findByAssigneeAndIsDeletedFalse(currentUser).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponse> getTaskStatusHistory(Long taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + taskId));

        validateTaskAccess(task, currentUser);

        return taskStatusHistoryRepository.findByTaskOrderByCreatedAtDesc(task)
                .stream()
                .map(history -> new TaskStatusHistoryResponse(
                        history.getId(),
                        history.getTask().getId(),
                        history.getStatus(),
                        history.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(Long projectId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı: id=" + projectId));

        validateProjectAccess(project, currentUser);

        List<Task> tasks = taskRepository.findByProjectAndIsDeletedFalse(project);

        long totalTasks = tasks.size();
        Map<TaskStatus, Long> countsByStatus = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        long todo = countsByStatus.getOrDefault(TaskStatus.TODO, 0L);
        long inProgress = countsByStatus.getOrDefault(TaskStatus.IN_PROGRESS, 0L);
        long done = countsByStatus.getOrDefault(TaskStatus.DONE, 0L);

        return new ProjectStatsResponse(totalTasks, todo, inProgress, done);
    }

    @Transactional
    public TaskResponse addLabelToTask(Long taskId, Long labelId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + taskId));

        validateTaskModificationAccess(task.getProject(), currentUser);
        
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new NotFoundException("Etiket bulunamadı. id = " + labelId));

        if (task.getLabels().contains(label)) {
            throw new AlreadyExistsException("Bu etiket zaten göreve eklenmiş.");
        }

        task.getLabels().add(label);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse removeLabelFromTask(Long taskId, Long labelId, Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + taskId));

        validateTaskModificationAccess(task.getProject(), currentUser);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new NotFoundException("Etiket bulunamadı. id = " + labelId));

        if (!task.getLabels().contains(label)) {
            throw new NotFoundException("Bu etiket görevde bulunmuyor.");
        }

        task.getLabels().remove(label);

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(Long taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task parentTask = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + taskId));

        validateTaskAccess(parentTask, currentUser);

        return taskRepository.findByParentTaskAndIsDeletedFalse(parentTask)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long projectId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı! ID: " + projectId));

        validateProjectAccess(project, currentUser);

        LocalDate today = LocalDate.now();

        return taskRepository.findByProjectAndIsDeletedFalse(project)
                .stream()
                .filter(task -> task.getDueDate() != null
                        && task.getDueDate().isBefore(today)
                        && task.getStatus() != TaskStatus.DONE)
                .map(this::mapToResponse)
                .toList();
    }

    private TaskResponse mapToResponse(Task task) {
    	List<LabelResponse> labelResponses = task.getLabels() != null
    	        ? task.getLabels().stream()
    	            .map(label -> new LabelResponse(
    	                    label.getId(),
    	                    label.getName(),
    	                    label.getColor(),
    	                    label.getCreatedAt(),
    	                    label.getUpdatedAt()
    	            ))
    	            .toList()
    	        : List.of();

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
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getName() : null,
                task.getParentTask() != null ? task.getParentTask().getId() : null,
                labelResponses
        );
    }
}