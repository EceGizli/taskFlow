package com.beat.taskFlow.task;

import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.dto.requests.CreateCheckItemRequest;
import com.beat.taskFlow.task.dto.responses.CheckItemResponse;
import com.beat.taskFlow.task.entity.concretes.CheckItem;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.CheckItemRepository;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.service.CheckItemService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckItemServiceTest {

    @Mock
    private CheckItemRepository checkItemRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CheckItemService checkItemService;

    private User user;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder().name("Test User").email("test@example.com").build();
        user.setId(1L);

        project = Project.builder().name("Test Project").owner(user).members(new HashSet<>()).build();
        project.setId(10L);

        task = Task.builder().title("Test Task").project(project).build();
        task.setId(100L);
    }

    @Test
    void createCheckItem_Success() {
        CreateCheckItemRequest request = new CreateCheckItemRequest("Checklist Maddesi 1");
        CheckItem checkItem = CheckItem.builder().title(request.title()).isCompleted(false).task(task).build();
        checkItem.setId(5L);

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(task));
        when(checkItemRepository.save(any(CheckItem.class))).thenReturn(checkItem);

        CheckItemResponse response = checkItemService.createCheckItem(100L, request, authentication);

        assertNotNull(response);
        assertEquals("Checklist Maddesi 1", response.title());
        assertFalse(response.isCompleted());
        verify(checkItemRepository, times(1)).save(any(CheckItem.class));
    }

    @Test
    void toggleCheckItem_Success() {
        CheckItem checkItem = CheckItem.builder().title("Checklist Maddesi 1").isCompleted(false).task(task).build();
        checkItem.setId(5L);

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(checkItemRepository.findById(5L)).thenReturn(Optional.of(checkItem));
        when(checkItemRepository.save(any(CheckItem.class))).thenReturn(checkItem);

        CheckItemResponse response = checkItemService.toggleCheckItem(5L, authentication);

        assertNotNull(response);
        assertTrue(checkItem.isCompleted());
        verify(checkItemRepository, times(1)).save(checkItem);
    }
}