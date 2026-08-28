package com.beat.taskFlow.task.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.dto.responses.AttachmentResponse;
import com.beat.taskFlow.task.entity.concretes.Attachment;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.AttachmentRepository;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Dosya yükleme dizini oluşturulamadı.", ex);
        }
    }

    @Transactional
    public AttachmentResponse uploadFile(Long taskId, MultipartFile file, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı: " + taskId));

        checkProjectAccess(task.getProject(), currentUser);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Boş dosya yüklenemez.");
        }

        String originalFileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "unnamed";

        String fileExtension = "";

        int dotIndex = originalFileName.lastIndexOf('.');

        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        Set<String> allowedExtensions = Set.of(
                ".pdf",
                ".png",
                ".jpg",
                ".jpeg",
                ".txt",
                ".doc",
                ".docx",
                ".xls",
                ".xlsx"
        );

        Set<String> allowedContentTypes = Set.of(
                "application/pdf",
                "image/png",
                "image/jpeg",
                "text/plain",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        String contentType = file.getContentType();

        if (!allowedExtensions.contains(fileExtension.toLowerCase())
                || contentType == null
                || !allowedContentTypes.contains(contentType)) {

            throw new IllegalArgumentException(
                    "Desteklenmeyen dosya türü."
            );
        }

        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .fileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .fileSize(file.getSize())
                    .filePath(targetLocation.toString())
                    .task(task)
                    .uploadedBy(currentUser)
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);
            return toResponse(savedAttachment);
        } catch (IOException ex) {
            throw new RuntimeException("Dosya kaydedilemedi: " + originalFileName, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTaskId(Long taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı: " + taskId));

        checkProjectAccess(task.getProject(), currentUser);

        return attachmentRepository.findByTaskIdOrderByIdAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long attachmentId, Authentication authentication) {
        Attachment attachment = getAttachmentWithAccessCheck(attachmentId, authentication);

        try {
            Path filePath = this.fileStorageLocation.resolve(attachment.getStoredFileName()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new NotFoundException("Dosya bulunamadı veya okunamıyor: " + attachment.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new NotFoundException("Dosya yolu geçersiz: " + attachment.getFileName());
        }
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentEntity(Long attachmentId, Authentication authentication) {
        return getAttachmentWithAccessCheck(attachmentId, authentication);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Dosya eki bulunamadı: " + attachmentId));

        Project project = attachment.getTask().getProject();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isUploader = attachment.getUploadedBy().getId().equals(currentUser.getId());

        if (!isOwner && !isUploader) {
            throw new AccessDeniedException("Bu dosya ekini silme yetkiniz yok.");
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(attachment.getStoredFileName()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }

        attachmentRepository.delete(attachment);
    }

    private Attachment getAttachmentWithAccessCheck(Long attachmentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Dosya eki bulunamadı: " + attachmentId));

        checkProjectAccess(attachment.getTask().getProject(), currentUser);
        return attachment;
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
    }

    private void checkProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers() != null && project.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu projenin görev dosyalarına erişim yetkiniz yok.");
        }
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getTask().getId(),
                attachment.getUploadedBy().getName(),
                attachment.getCreatedAt()
        );
    }
}