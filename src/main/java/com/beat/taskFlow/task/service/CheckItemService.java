package com.beat.taskFlow.task.service;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.concretes.ProjectMember;
import com.beat.taskFlow.project.entity.enums.ProjectRole;
import com.beat.taskFlow.project.repository.ProjectMemberRepository;
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

@Service
@RequiredArgsConstructor
public class CheckItemService {

    private final CheckItemRepository checkItemRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public CheckItemResponse createCheckItem(Long taskId, CreateCheckItemRequest request, Authentication authentication) {
        User user = getUser(authentication);
        Task task = getTaskById(taskId);
        validateWriteAccess(task.getProject(), user);

        CheckItem checkItem = CheckItem.builder()
                .title(request.title())
                .isCompleted(false)
                .task(task)
                .build();

        return mapToResponse(checkItemRepository.save(checkItem));
    }

    @Transactional(readOnly = true)
    public List<CheckItemResponse> getCheckItemsByTaskId(Long taskId, Authentication authentication) {
        User user = getUser(authentication);
        Task task = getTaskById(taskId);
        checkProjectAccess(task.getProject(), user);

        return checkItemRepository.findByTaskIdOrderByIdAsc(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CheckItemResponse updateCheckItem(Long checkItemId, UpdateCheckItemRequest request, Authentication authentication) {
        User user = getUser(authentication);
        CheckItem checkItem = getCheckItemById(checkItemId);
        validateWriteAccess(checkItem.getTask().getProject(), user);

        if (request.title() != null && !request.title().isBlank()) {
            checkItem.setTitle(request.title());
        }
        if (request.isCompleted() != null) {
            checkItem.setCompleted(request.isCompleted());
        }

        return mapToResponse(checkItemRepository.save(checkItem));
    }

    @Transactional
    public CheckItemResponse toggleCheckItem(Long checkItemId, Authentication authentication) {
        User user = getUser(authentication);
        CheckItem checkItem = getCheckItemById(checkItemId);
        validateWriteAccess(checkItem.getTask().getProject(), user);

        checkItem.setCompleted(!checkItem.isCompleted());
        return mapToResponse(checkItemRepository.save(checkItem));
    }

    @Transactional
    public void deleteCheckItem(Long checkItemId, Authentication authentication) {
        User user = getUser(authentication);
        CheckItem checkItem = getCheckItemById(checkItemId);
        validateWriteAccess(checkItem.getTask().getProject(), user);

        checkItemRepository.delete(checkItem);
    }

    private void checkProjectAccess(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getMembers() != null && project.getMembers().contains(user);

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Bu projeye erişim yetkiniz yok.");
        }
    }

    private void validateWriteAccess(Project project, User user) {
        if (project.getOwner() != null && project.getOwner().getId().equals(user.getId())) {
            return;
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bu projeye erişim yetkiniz yok."));

        if (member.getRole() == ProjectRole.VIEWER) {
            throw new AccessDeniedException("VIEWER rolündeki üyeler checklist üzerinde değişiklik yapamaz.");
        }
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı."));
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Görev bulunamadı."));
    }

    private CheckItem getCheckItemById(Long checkItemId) {
        return checkItemRepository.findById(checkItemId)
                .orElseThrow(() -> new NotFoundException("Kontrol maddesi bulunamadı."));
    }

    private CheckItemResponse mapToResponse(CheckItem checkItem) {
        return new CheckItemResponse(
                checkItem.getId(),
                checkItem.getTitle(),
                checkItem.isCompleted(),
                checkItem.getTask().getId(),
                checkItem.getCreatedAt(),
                checkItem.getUpdatedAt()
        );
    }
}