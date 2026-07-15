package com.beat.taskFlow.task.service;

import com.beat.taskFlow.common.exception.InvalidTaskStatusException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.dto.responses.TaskResponse;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı. id = " + projectId));

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .project(project)
                .build();

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    public List<TaskResponse> getTasksByProjectId(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proje bulunamadı. id = " + projectId));

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus = request.status();

        validateStatusTransition(currentStatus, newStatus);

        task.setStatus(newStatus);

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı. id = " + id));

        taskRepository.delete(task);
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {

        if (currentStatus == TaskStatus.DONE &&
                newStatus != TaskStatus.DONE) {

            throw new InvalidTaskStatusException(
                    "Tamamlanan görev tekrar başka bir duruma geçirilemez.");
        }

        if (currentStatus == TaskStatus.TODO &&
                newStatus == TaskStatus.DONE) {

            throw new InvalidTaskStatusException(
                    "Görev tamamlanmadan önce IN_PROGRESS durumuna geçirilmelidir.");
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
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}