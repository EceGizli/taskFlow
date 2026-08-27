package com.beat.taskFlow.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.concretes.ProjectMember;
import com.beat.taskFlow.project.entity.enums.ProjectRole;
import com.beat.taskFlow.project.repository.ProjectMemberRepository;
import com.beat.taskFlow.task.service.TaskService;
import com.beat.taskFlow.user.entity.concretes.User;

@ExtendWith(MockitoExtension.class)
class ProjectRoleSecurityTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private User owner;
    private User memberUser;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");

        memberUser = new User();
        memberUser.setId(2L);
        memberUser.setEmail("member@test.com");

        project = new Project();
        project.setId(10L);
        project.setOwner(owner);
    }

    @Test
    void validateTaskModificationAccess_WhenUserIsViewer_ShouldThrowAccessDeniedException() {
        ProjectMember viewerMember = ProjectMember.builder()
                .project(project)
                .user(memberUser)
                .role(ProjectRole.VIEWER)
                .build();

        when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.of(viewerMember));

        assertThrows(AccessDeniedException.class, () -> 
            taskService.validateTaskModificationAccess(project, memberUser)
        );
    }

    @Test
    void validateTaskModificationAccess_WhenUserIsEditor_ShouldAllowAccess() {
        ProjectMember editorMember = ProjectMember.builder()
                .project(project)
                .user(memberUser)
                .role(ProjectRole.EDITOR)
                .build();

        when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.of(editorMember));

        assertDoesNotThrow(() -> 
            taskService.validateTaskModificationAccess(project, memberUser)
        );
    }

    @Test
    void validateTaskModificationAccess_WhenUserIsOwner_ShouldAllowAccess() {
        assertDoesNotThrow(() -> 
            taskService.validateTaskModificationAccess(project, owner)
        );
    }
}