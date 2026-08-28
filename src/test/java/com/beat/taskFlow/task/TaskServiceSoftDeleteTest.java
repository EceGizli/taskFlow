package com.beat.taskFlow.task;

import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.repository.TaskStatusHistoryRepository;
import com.beat.taskFlow.task.service.TaskService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import java.util.HashSet;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceSoftDeleteTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskStatusHistoryRepository taskStatusHistoryRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTaskById_shouldThrowNotFound_whenTaskIsDeleted() {
        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createUser()));

        when(taskRepository.findByIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.getTaskById(10L, authentication));
    }

    @Test
    void updateTask_shouldThrowNotFound_whenTaskIsDeleted() {
        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createUser()));

        when(taskRepository.findByIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.updateTask(10L, null, authentication));
    }

    @Test
    void updateTaskStatus_shouldThrowNotFound_whenTaskIsDeleted() {
        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createUser()));

        when(taskRepository.findByIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.updateTaskStatus(10L, null, authentication));
    }

    @Test
    void updateTaskAssignee_shouldThrowNotFound_whenTaskIsDeleted() {
        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createUser()));

        when(taskRepository.findByIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.updateTaskAssignee(10L, null, authentication));
    }

    @Test
    void getSubtasks_shouldThrowNotFound_whenParentTaskIsDeleted() {
        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createUser()));

        when(taskRepository.findByIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.getSubtasks(10L, authentication));

        verify(taskRepository, never()).findByParentTaskAndIsDeletedFalse(any());
    }

    @Test
    void getTasksAssignedToMe_shouldUseOnlyNonDeletedTasks() {
        User user = createUser();

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        Project project = Project.builder()
                .owner(user)
                .members(new HashSet<>())
                .build();

        Task activeTask = Task.builder()
                .title("Active Task")
                .project(project)
                .isDeleted(false)
                .build();

        when(taskRepository.findByAssigneeAndIsDeletedFalse(user))
                .thenReturn(java.util.List.of(activeTask));

        var result = taskService.getTasksAssignedToMe(authentication);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByAssigneeAndIsDeletedFalse(user);
        verify(taskRepository, never()).findByAssignee(user);
    }

    @Test
    void getOverdueTasks_shouldUseOnlyNonDeletedTasks() {
        User user = createUser();

        Project project = Project.builder()
                .owner(user)
                .members(new HashSet<>())
                .build();

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(projectRepository.findByIdAndIsDeletedFalse(5L)).thenReturn(Optional.of(project));
        
        when(taskRepository.findByProjectAndIsDeletedFalse(project))
                .thenReturn(java.util.List.of());

        var result = taskService.getOverdueTasks(5L, authentication);

        assertThat(result).isEmpty();
        verify(taskRepository).findByProjectAndIsDeletedFalse(project);
        verify(taskRepository, never()).findByProject(project);
    }

    private User createUser() {
        User user = User.builder()
                .email("test@mail.com")
                .build();
        user.setId(1L);
        return user;
    }
}