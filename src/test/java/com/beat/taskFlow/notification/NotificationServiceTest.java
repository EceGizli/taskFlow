package com.beat.taskFlow.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.beat.taskFlow.notification.dto.responses.NotificationResponse;
import com.beat.taskFlow.notification.entity.concretes.Notification;
import com.beat.taskFlow.notification.repository.NotificationRepository;
import com.beat.taskFlow.notification.service.NotificationService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@ornek.com");

        notification = Notification.builder()
                .user(user)
                .title("Test Bildirimi")
                .message("Test Mesajı")
                .isRead(false)
                .build();
        notification.setId(10L);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_ShouldSaveNotification() {
        notificationService.createNotification(user, "Başlık", "Mesaj");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getMyNotifications_ShouldReturnPagedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserId(user.getId(), pageable)).thenReturn(page);

        Page<NotificationResponse> result = notificationService.getMyNotifications(authentication, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Bildirimi", result.getContent().get(0).title());
    }

    @Test
    void markAsRead_WhenOwner_ShouldSetReadTrue() {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(10L, authentication);

        assertNotNull(response);
        assertTrue(notification.isRead());
    }

    @Test
    void markAsRead_WhenNotOwner_ShouldThrowAccessDeniedException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@ornek.com");

        when(authentication.getName()).thenReturn(anotherUser.getEmail());
        when(userRepository.findByEmail(anotherUser.getEmail())).thenReturn(Optional.of(anotherUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(10L, authentication));
    }
}