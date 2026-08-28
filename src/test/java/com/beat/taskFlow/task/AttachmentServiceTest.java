package com.beat.taskFlow.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.dto.responses.AttachmentResponse;
import com.beat.taskFlow.task.entity.concretes.Attachment;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.AttachmentRepository;
import com.beat.taskFlow.task.repository.TaskRepository;
import com.beat.taskFlow.task.service.AttachmentService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @TempDir
    Path tempUploadDir;

    private AttachmentService attachmentService;
    private User user;
    private Project project;
    private Task task;
    private Attachment attachment;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService(
                attachmentRepository,
                taskRepository,
                userRepository,
                tempUploadDir.toString()
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
        task.setProject(project);

        attachment = Attachment.builder()
                .fileName("test.txt")
                .storedFileName("stored-test.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .filePath(tempUploadDir.resolve("stored-test.txt").toString())
                .task(task)
                .uploadedBy(user)
                .build();
        attachment.setId(10L);
    }

    @Test
    void uploadFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "belge.pdf",
                "application/pdf",
                "Test içerik".getBytes()
        );

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> {
            Attachment a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AttachmentResponse response = attachmentService.uploadFile(1L, file, authentication);

        assertNotNull(response);
        assertEquals("belge.pdf", response.fileName());
        assertEquals("application/pdf", response.fileType());
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void uploadFile_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () ->
                attachmentService.uploadFile(1L, file, authentication)
        );
    }

    @Test
    void getAttachmentsByTaskId_Success() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(attachmentRepository.findByTaskIdOrderByIdAsc(1L)).thenReturn(List.of(attachment));

        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTaskId(1L, authentication);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("test.txt", responses.get(0).fileName());
    }

    @Test
    void deleteAttachment_Success() throws Exception {
        Path filePath = tempUploadDir.resolve("stored-test.txt");
        Files.createFile(filePath);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(attachmentRepository.findById(10L)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(10L, authentication);

        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_Unauthorized_ThrowsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@taskflow.com");

        when(authentication.getName()).thenReturn(otherUser.getEmail());
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(attachmentRepository.findById(10L)).thenReturn(Optional.of(attachment));

        assertThrows(AccessDeniedException.class, () ->
                attachmentService.deleteAttachment(10L, authentication)
        );
    }

    @Test
    void deleteAttachment_NotFound_ThrowsException() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(attachmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                attachmentService.deleteAttachment(99L, authentication)
        );
    }
    
    @Test
    void uploadFile_UnsupportedFileType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "test".getBytes()
        );

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () ->
                attachmentService.uploadFile(1L, file, authentication)
        );

        verify(attachmentRepository, org.mockito.Mockito.never())
                .save(any(Attachment.class));
    }
    
    @Test
    void uploadFile_AllowedFileType_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "test image".getBytes()
        );

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> {
            Attachment a = inv.getArgument(0);
            a.setId(11L);
            return a;
        });

        AttachmentResponse response =
                attachmentService.uploadFile(1L, file, authentication);

        assertNotNull(response);
        assertEquals("image.png", response.fileName());
        assertEquals("image/png", response.fileType());

        verify(attachmentRepository).save(any(Attachment.class));
    }
}