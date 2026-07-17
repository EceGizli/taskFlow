package com.beat.taskFlow.task.service;

import com.beat.taskFlow.common.exception.InvalidTaskStatusException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.dto.requests.BulkUpdateStatusRequest;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı. id = " + projectId));

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
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı. id = " + projectId));

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setEstimatedHours(request.estimatedHours());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        validateStatusTransition(task.getStatus(), request.status());

        task.setStatus(request.status());
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public List<TaskResponse> bulkUpdateStatus(BulkUpdateStatusRequest request) {
        List<Task> tasksToUpdate = new ArrayList<>();
        
        for (Long id : request.taskIds()) {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Toplu güncelleme sırasında görev bulunamadı. id = " + id));
            
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
    public TaskResponse duplicateTask(Long id) {
        Task sourceTask = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kopyalanacak görev bulunamadı. id = " + id));

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
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        taskRepository.delete(task);
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
                task.getUpdatedAt()
        );
    }
}