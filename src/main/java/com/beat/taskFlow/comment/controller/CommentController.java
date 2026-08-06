package com.beat.taskFlow.comment.controller;

import com.beat.taskFlow.comment.dto.requests.CreateCommentRequest;
import com.beat.taskFlow.comment.dto.requests.UpdateCommentRequest;
import com.beat.taskFlow.comment.dto.responses.CommentResponse;
import com.beat.taskFlow.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        return commentService.createComment(taskId, request, authentication);
    }

    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponse> getCommentsByTask(
            @PathVariable Long taskId,
            Authentication authentication) {

        return commentService.getCommentsByTask(taskId, authentication);
    }
    
    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        return commentService.updateComment(commentId, request, authentication);
    }
    
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        commentService.deleteComment(commentId, authentication);
    }
}