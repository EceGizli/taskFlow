package com.beat.taskFlow.task;

import com.beat.taskFlow.common.exception.InvalidTaskStatusException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.task.dto.requests.CreateTaskRequest;
import com.beat.taskFlow.task.dto.requests.UpdateTaskStatusRequest;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.repository.TaskStatusHistoryRepository;
import com.beat.taskFlow.task.service.TaskService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceStatusTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskStatusHistoryRepository taskStatusHistoryRepository;
    @Mock private Authentication authentication;

    @InjectMocks private TaskService taskService;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = User.builder().email("u@test.com").build();
        user.setId(1L);

        project = Project.builder().owner(user).members(new HashSet<>()).build();
        project.setId(5L);
    }

    @Test
    void updateTaskStatus_todoToDone_throwsInvalidTransition() {
        Task task = Task.builder().status(TaskStatus.TODO).project(project).build();
        task.setId(100L);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
                taskService.updateTaskStatus(100L, new UpdateTaskStatusRequest(TaskStatus.DONE), authentication)
        ).isInstanceOf(InvalidTaskStatusException.class);
    }

    @Test
    void createTask_assigneeNotMember_throwsAccessDenied() {
        User outsider = User.builder().build();
        outsider.setId(9L);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(userRepository.findById(9L)).thenReturn(Optional.of(outsider));

        CreateTaskRequest req = new CreateTaskRequest("title", "desc", null, null, null, null, 9L, null);

        assertThatThrownBy(() -> taskService.createTask(5L, req, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTaskStatusHistory_userNotProjectMember_throwsAccessDenied() {
        User outsider = User.builder().email("out@test.com").build();
        outsider.setId(9L);

        Task task = Task.builder().status(TaskStatus.TODO).project(project).build();
        task.setId(100L);

        when(authentication.getName()).thenReturn(outsider.getEmail());
        when(userRepository.findByEmail(outsider.getEmail())).thenReturn(Optional.of(outsider));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getTaskStatusHistory(100L, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }
}