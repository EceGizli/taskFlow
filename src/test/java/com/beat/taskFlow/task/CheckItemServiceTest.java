package com.beat.taskFlow.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.concretes.ProjectMember;
import com.beat.taskFlow.project.entity.enums.ProjectRole;
import com.beat.taskFlow.project.repository.ProjectMemberRepository;
import com.beat.taskFlow.task.dto.requests.CreateCheckItemRequest;
import com.beat.taskFlow.task.dto.requests.UpdateCheckItemRequest;
import com.beat.taskFlow.task.dto.responses.CheckItemResponse;
import com.beat.taskFlow.task.entity.concretes.CheckItem;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.CheckItemRepository;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.service.CheckItemService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CheckItemServiceTest {

    @Mock
    private CheckItemRepository checkItemRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CheckItemService checkItemService;

    private User user;
    private Project project;
    private Task task;
    private CheckItem checkItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@taskflow.com");

        project = new Project();
        project.setId(1L);
        project.setOwner(user);
        project.setMembers(Collections.emptySet());

        task = new Task();
        task.setId(1L);
        task.setProject(project);

        checkItem = CheckItem.builder()
                .title("Test Check Item")
                .isCompleted(false)
                .task(task)
                .build();
        checkItem.setId(10L);
    }

    @Test
    void createCheckItem_Success() {
        CreateCheckItemRequest request = new CreateCheckItemRequest("Yeni Madde");

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(checkItemRepository.save(any(CheckItem.class))).thenAnswer(invocation -> {
            CheckItem item = invocation.getArgument(0);
            item.setId(10L);
            return item;
        });

        CheckItemResponse response = checkItemService.createCheckItem(1L, request, authentication);

        assertNotNull(response);
        assertEquals("Yeni Madde", response.title());
        assertFalse(response.isCompleted());
        verify(checkItemRepository).save(any(CheckItem.class));
    }

    @Test
    void getCheckItemsByTaskId_Success() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(checkItemRepository.findByTaskIdOrderByIdAsc(1L)).thenReturn(List.of(checkItem));

        List<CheckItemResponse> response = checkItemService.getCheckItemsByTaskId(1L, authentication);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Test Check Item", response.get(0).title());
    }

    @Test
    void toggleCheckItem_Success() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(checkItemRepository.findById(10L)).thenReturn(Optional.of(checkItem));
        when(checkItemRepository.save(any(CheckItem.class))).thenReturn(checkItem);

        CheckItemResponse response = checkItemService.toggleCheckItem(10L, authentication);

        assertNotNull(response);
        assertTrue(response.isCompleted());
        verify(checkItemRepository).save(checkItem);
    }

    @Test
    void updateCheckItem_Success() {
        UpdateCheckItemRequest request = new UpdateCheckItemRequest("Güncel Başlık", true);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(checkItemRepository.findById(10L)).thenReturn(Optional.of(checkItem));
        when(checkItemRepository.save(any(CheckItem.class))).thenReturn(checkItem);

        CheckItemResponse response = checkItemService.updateCheckItem(10L, request, authentication);

        assertNotNull(response);
        assertEquals("Güncel Başlık", response.title());
        assertTrue(response.isCompleted());
        verify(checkItemRepository).save(checkItem);
    }

    @Test
    void deleteCheckItem_Success() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(checkItemRepository.findById(10L)).thenReturn(Optional.of(checkItem));

        checkItemService.deleteCheckItem(10L, authentication);

        verify(checkItemRepository).delete(checkItem);
    }

    @Test
    void checkProjectAccess_AccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@taskflow.com");

        when(authentication.getName()).thenReturn(otherUser.getEmail());
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CreateCheckItemRequest request = new CreateCheckItemRequest("Yetkisiz İstek");

        assertThrows(AccessDeniedException.class, () ->
                checkItemService.createCheckItem(1L, request, authentication)
        );
    }

    @Test
    void createCheckItem_ViewerRole_ThrowsAccessDenied() {
        User viewerUser = new User();
        viewerUser.setId(3L);
        viewerUser.setEmail("viewer@taskflow.com");

        when(authentication.getName()).thenReturn(viewerUser.getEmail());
        when(userRepository.findByEmail(viewerUser.getEmail())).thenReturn(Optional.of(viewerUser));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 3L)).thenReturn(Optional.of(
                ProjectMember.builder().project(project).user(viewerUser).role(ProjectRole.VIEWER).build()
        ));

        CreateCheckItemRequest request = new CreateCheckItemRequest("Yetkisiz İstek");

        assertThrows(AccessDeniedException.class, () ->
                checkItemService.createCheckItem(1L, request, authentication)
        );
    }

    @Test
    void getCheckItemById_NotFound() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(checkItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                checkItemService.toggleCheckItem(99L, authentication)
        );
    }
}