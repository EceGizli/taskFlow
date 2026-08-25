package com.beat.taskFlow.task.service;

import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.dto.requests.CreateCheckItemRequest;
import com.beat.taskFlow.task.dto.requests.UpdateCheckItemRequest;
import com.beat.taskFlow.task.dto.responses.CheckItemResponse;
import com.beat.taskFlow.task.entity.concretes.CheckItem;
import com.beat.taskFlow.task.entity.concretes.Task;
import com.beat.taskFlow.task.repository.CheckItemRepository;
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
public class CheckItemService {

    private final CheckItemRepository checkItemRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public CheckItemResponse createCheckItem(Long taskId, CreateCheckItemRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + taskId));

        validateTaskAccess(task, currentUser);

        CheckItem checkItem = CheckItem.builder()
                .title(request.title().trim())
                .isCompleted(false)
                .task(task)
                .build();

        CheckItem saved = checkItemRepository.save(checkItem);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CheckItemResponse> getCheckItemsByTaskId(Long taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı! ID: " + taskId));

        validateTaskAccess(task, currentUser);

        return checkItemRepository.findByTaskOrderByIdAsc(task)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CheckItemResponse updateCheckItem(Long checkItemId, UpdateCheckItemRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CheckItem checkItem = checkItemRepository.findById(checkItemId)
                .orElseThrow(() -> new NotFoundException("Kontrol maddesi bulunamadı! ID: " + checkItemId));

        validateTaskAccess(checkItem.getTask(), currentUser);

        if (request.title() != null && !request.title().trim().isEmpty()) {
            checkItem.setTitle(request.title().trim());
        }
        if (request.isCompleted() != null) {
            checkItem.setCompleted(request.isCompleted());
        }

        CheckItem updated = checkItemRepository.save(checkItem);
        return mapToResponse(updated);
    }

    @Transactional
    public CheckItemResponse toggleCheckItem(Long checkItemId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CheckItem checkItem = checkItemRepository.findById(checkItemId)
                .orElseThrow(() -> new NotFoundException("Kontrol maddesi bulunamadı! ID: " + checkItemId));

        validateTaskAccess(checkItem.getTask(), currentUser);

        checkItem.setCompleted(!checkItem.isCompleted());
        CheckItem updated = checkItemRepository.save(checkItem);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCheckItem(Long checkItemId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CheckItem checkItem = checkItemRepository.findById(checkItemId)
                .orElseThrow(() -> new NotFoundException("Kontrol maddesi bulunamadı! ID: " + checkItemId));

        validateTaskAccess(checkItem.getTask(), currentUser);

        checkItemRepository.delete(checkItem);
    }

    private void validateTaskAccess(Task task, User user) {
        Project project = task.getProject();
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu göreve erişim yetkiniz yok!");
        }
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Giriş yapan kullanıcı bulunamadı!"));
    }

    private CheckItemResponse mapToResponse(CheckItem item) {
        return new CheckItemResponse(
                item.getId(),
                item.getTitle(),
                item.isCompleted(),
                item.getTask().getId(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}