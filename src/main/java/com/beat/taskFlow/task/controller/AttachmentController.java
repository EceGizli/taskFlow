package com.beat.taskFlow.task.controller;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.beat.taskFlow.task.dto.responses.AttachmentResponse;
import com.beat.taskFlow.task.entity.concretes.Attachment;
import com.beat.taskFlow.task.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Attachments", description = "Görev dosya eki (Attachment) yönetim uçları")
@SecurityRequirement(name = "bearerAuth")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Göreve dosya yükle")
    public ResponseEntity<AttachmentResponse> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        AttachmentResponse response = attachmentService.uploadFile(taskId, file, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks/{taskId}/attachments")
    @Operation(summary = "Görevin dosya eklerini listele")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Long taskId,
            Authentication authentication) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTaskId(taskId, authentication));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Dosya ekini indir")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long attachmentId,
            Authentication authentication) {
        Resource resource = attachmentService.downloadFile(attachmentId, authentication);
        Attachment attachment = attachmentService.getAttachmentEntity(attachmentId, authentication);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Dosya ekini sil")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication) {
        attachmentService.deleteAttachment(attachmentId, authentication);
        return ResponseEntity.noContent().build();
    }
}