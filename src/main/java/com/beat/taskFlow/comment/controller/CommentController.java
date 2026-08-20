package com.beat.taskFlow.comment.controller;

import com.beat.taskFlow.comment.dto.requests.CreateCommentRequest;
import com.beat.taskFlow.comment.dto.requests.UpdateCommentRequest;
import com.beat.taskFlow.comment.dto.responses.CommentResponse;
import com.beat.taskFlow.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Görev yorumları yönetimi uçları")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Göreve yorum ekle", description = "Belirtilen göreve yeni bir yorum ekler.")
    @PostMapping("/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        return commentService.createComment(taskId, request, authentication);
    }

    @Operation(summary = "Görevin yorumlarını listele", description = "Belirtilen göreve ait tüm yorumları listeler.")
    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponse> getCommentsByTask(
            @PathVariable Long taskId,
            Authentication authentication) {

        return commentService.getCommentsByTask(taskId, authentication);
    }

    @Operation(summary = "Yorumu güncelle", description = "Yorumun içeriğini günceller (Sadece yorum sahibi).")
    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        return commentService.updateComment(commentId, request, authentication);
    }

    @Operation(summary = "Yorumu sil", description = "Belirtilen yorumu siler (Yorum sahibi veya proje sahibi).")
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        commentService.deleteComment(commentId, authentication);
    }
}