package com.beat.taskFlow.task;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.label.entity.Label;
import com.beat.taskFlow.label.repository.LabelRepository;
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
class TaskServiceLabelTest {

@Mock
private TaskRepository taskRepository;

@Mock
private ProjectRepository projectRepository;

@Mock
private UserRepository userRepository;

@Mock
private TaskStatusHistoryRepository taskStatusHistoryRepository;

@Mock
private LabelRepository labelRepository;

@Mock
private Authentication authentication;

@Mock
private User user;

@Mock
private Project project;

@Mock
private Task task;

@Mock
private Label label;

@InjectMocks
private TaskService taskService;

@Test
void addLabelToTask_shouldAddLabel_whenTaskAndLabelAreValid() {

    when(authentication.getName()).thenReturn("test@mail.com");
    when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

    when(user.getId()).thenReturn(1L);

    when(taskRepository.findByIdAndIsDeletedFalse(10L))
            .thenReturn(Optional.of(task));

    when(task.getProject()).thenReturn(project);
    when(project.getOwner()).thenReturn(user);

    when(labelRepository.findById(20L))
            .thenReturn(Optional.of(label));

    HashSet<Label> labels = new HashSet<>();
    when(task.getLabels()).thenReturn(labels);

    when(taskRepository.save(task)).thenReturn(task);

    taskService.addLabelToTask(10L, 20L, authentication);

    assertThat(task.getLabels()).contains(label);

    verify(taskRepository).save(task);
    verify(labelRepository).findById(20L);
}

@Test
void removeLabelFromTask_shouldRemoveLabel_whenTaskAndLabelAreValid() {

    when(authentication.getName()).thenReturn("test@mail.com");
    when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

    when(user.getId()).thenReturn(1L);

    when(taskRepository.findByIdAndIsDeletedFalse(10L))
            .thenReturn(Optional.of(task));

    when(task.getProject()).thenReturn(project);
    when(project.getOwner()).thenReturn(user);

    when(labelRepository.findById(20L))
            .thenReturn(Optional.of(label));

    HashSet<Label> labels = new HashSet<>();
    labels.add(label);

    when(task.getLabels()).thenReturn(labels);

    when(taskRepository.save(task)).thenReturn(task);

    taskService.removeLabelFromTask(10L, 20L, authentication);

    assertThat(task.getLabels()).doesNotContain(label);

    verify(taskRepository).save(task);
    verify(labelRepository).findById(20L);
}

@Test
void addLabelToTask_shouldThrowAlreadyExistsException_whenLabelAlreadyExists() {

    when(authentication.getName()).thenReturn("test@mail.com");
    when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

    when(user.getId()).thenReturn(1L);

    when(taskRepository.findByIdAndIsDeletedFalse(10L))
            .thenReturn(Optional.of(task));

    when(task.getProject()).thenReturn(project);
    when(project.getOwner()).thenReturn(user);

    when(labelRepository.findById(20L))
            .thenReturn(Optional.of(label));

    HashSet<Label> labels = new HashSet<>();
    labels.add(label);

    when(task.getLabels()).thenReturn(labels);

    assertThrows(
            AlreadyExistsException.class,
            () -> taskService.addLabelToTask(10L, 20L, authentication)
    );

    verify(taskRepository, never()).save(task);
}

@Test
void removeLabelFromTask_shouldThrowNotFoundException_whenLabelDoesNotExistOnTask() {

    when(authentication.getName()).thenReturn("test@mail.com");
    when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

    when(user.getId()).thenReturn(1L);

    when(taskRepository.findByIdAndIsDeletedFalse(10L))
            .thenReturn(Optional.of(task));

    when(task.getProject()).thenReturn(project);
    when(project.getOwner()).thenReturn(user);

    when(labelRepository.findById(20L))
            .thenReturn(Optional.of(label));

    when(task.getLabels()).thenReturn(new HashSet<>());

    assertThrows(
            NotFoundException.class,
            () -> taskService.removeLabelFromTask(10L, 20L, authentication)
    );

    verify(taskRepository, never()).save(task);
}

}
