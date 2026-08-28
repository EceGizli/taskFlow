package com.beat.taskFlow.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.notification.dto.responses.NotificationResponse;
import com.beat.taskFlow.notification.entity.concretes.Notification;
import com.beat.taskFlow.notification.repository.NotificationRepository;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User recipient, String title, String message) {
        if (recipient == null) return;
        Notification notification = Notification.builder()
                .user(recipient)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Authentication authentication, Pageable pageable) {
        User user = getCurrentUser(authentication);
        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Bildirim bulunamadı: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bu bildirimi okuma yetkiniz yok.");
        }

        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);

        notificationRepository.findByUserIdAndIsReadFalse(user.getId())
                .forEach(notification -> notification.setRead(true));
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}