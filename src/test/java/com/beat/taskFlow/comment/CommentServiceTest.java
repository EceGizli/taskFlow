package com.beat.taskFlow.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.beat.taskFlow.comment.dto.requests.CreateCommentRequest;
import com.beat.taskFlow.comment.dto.requests.UpdateCommentRequest;
import com.beat.taskFlow.comment.dto.responses.CommentResponse;
import com.beat.taskFlow.comment.entity.Comment;
import com.beat.taskFlow.comment.repository.CommentRepository;
import com.beat.taskFlow.comment.service.CommentService;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.notification.service.NotificationService;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.concretes.ProjectMember;
import com.beat.taskFlow.project.entity.enums.ProjectRole;
import com.beat.taskFlow.project.repository.ProjectMemberRepository;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private Authentication authentication;

    private CommentService commentService;
    private User user;
    private Project project;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
                commentRepository,
                taskRepository,
                userRepository,
                notificationService,
                projectMemberRepository
        );

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@taskflow.com");

        project = new Project();
        project.setId(1L);
        project.setOwner(user);
        project.setMembers(Collections.emptySet());

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setProject(project);

        comment = Comment.builder()
                .content("Eski yorum")
                .task(task)
                .author(user)
                .build();
        comment.setId(10L);
    }

    @Test
    void createComment_Success() {
        CreateCommentRequest request =
                new CreateCommentRequest("Yeni yorum");

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        CommentResponse response =
                commentService.createComment(1L, request, authentication);

        assertNotNull(response);
        assertEquals("Yeni yorum", response.content());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_TaskNotFound_ThrowsException() {
        CreateCommentRequest request =
                new CreateCommentRequest("Yeni yorum");

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                commentService.createComment(99L, request, authentication)
        );
    }

    @Test
    void createComment_UserWithoutProjectAccess_ThrowsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@taskflow.com");

        CreateCommentRequest request =
                new CreateCommentRequest("Yeni yorum");

        when(authentication.getName()).thenReturn(otherUser.getEmail());
        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () ->
                commentService.createComment(1L, request, authentication)
        );
    }

    @Test
    void createComment_ViewerRole_ThrowsAccessDenied() {
        User viewerUser = new User();
        viewerUser.setId(3L);
        viewerUser.setEmail("viewer@taskflow.com");

        CreateCommentRequest request = new CreateCommentRequest("Yeni yorum");

        when(authentication.getName()).thenReturn(viewerUser.getEmail());
        when(userRepository.findByEmail(viewerUser.getEmail()))
                .thenReturn(Optional.of(viewerUser));
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 3L))
                .thenReturn(Optional.of(
                        com.beat.taskFlow.project.entity.concretes.ProjectMember.builder()
                                .project(project)
                                .user(viewerUser)
                                .role(com.beat.taskFlow.project.entity.enums.ProjectRole.VIEWER)
                                .build()
                ));

        assertThrows(AccessDeniedException.class, () ->
                commentService.createComment(1L, request, authentication)
        );

        verify(commentRepository, org.mockito.Mockito.never()).save(any(Comment.class));
    }

    @Test
    void createComment_EditorRole_Success() {
        User editorUser = new User();
        editorUser.setId(3L);
        editorUser.setEmail("editor@taskflow.com");

        CreateCommentRequest request = new CreateCommentRequest("Yeni yorum");

        when(authentication.getName()).thenReturn(editorUser.getEmail());
        when(userRepository.findByEmail(editorUser.getEmail()))
                .thenReturn(Optional.of(editorUser));
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 3L))
                .thenReturn(Optional.of(
                        com.beat.taskFlow.project.entity.concretes.ProjectMember.builder()
                                .project(project)
                                .user(editorUser)
                                .role(com.beat.taskFlow.project.entity.enums.ProjectRole.EDITOR)
                                .build()
                ));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment saved = invocation.getArgument(0);
                    saved.setId(11L);
                    return saved;
                });

        CommentResponse response = commentService.createComment(1L, request, authentication);

        assertNotNull(response);
        assertEquals("Yeni yorum", response.content());
    }

    @Test
    void updateComment_ByAuthor_Success() {
        UpdateCommentRequest request =
                new UpdateCommentRequest("Güncellenmiş yorum");

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentResponse response =
                commentService.updateComment(10L, request, authentication);

        assertEquals("Güncellenmiş yorum", response.content());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_ByAnotherUser_ThrowsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@taskflow.com");

        UpdateCommentRequest request =
                new UpdateCommentRequest("Yetkisiz güncelleme");

        when(authentication.getName()).thenReturn(otherUser.getEmail());
        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () ->
                commentService.updateComment(10L, request, authentication)
        );
    }
}