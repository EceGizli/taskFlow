package com.beat.taskFlow.comment.service;

import com.beat.taskFlow.comment.dto.requests.CreateCommentRequest;
import com.beat.taskFlow.comment.dto.responses.CommentResponse;
import com.beat.taskFlow.comment.entity.Comment;
import com.beat.taskFlow.comment.repository.CommentRepository;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private void validateTaskAccess(Task task, User user) {

        Project project = task.getProject();

        boolean isOwner = project.getOwner().getId().equals(user.getId());

        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException(
                    "Bu göreve erişim yetkiniz bulunmamaktadır.");
        }
    }

    @Transactional
    public CommentResponse createComment(Long taskId,
                                         CreateCommentRequest request,
                                         Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException("Görev bulunamadı. id = " + taskId));

        validateTaskAccess(task, currentUser);

        Comment comment = Comment.builder()
                .content(request.content())
                .task(task)
                .author(currentUser)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return mapToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(Long taskId,
                                                   Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException("Görev bulunamadı. id = " + taskId));

        validateTaskAccess(task, currentUser);

        return commentRepository.findByTaskId(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CommentResponse mapToResponse(Comment comment) {

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}