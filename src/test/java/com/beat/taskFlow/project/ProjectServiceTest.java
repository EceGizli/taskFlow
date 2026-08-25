package com.beat.taskFlow.project;

import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.TransferOwnershipRequest;
import com.beat.taskFlow.project.dto.requests.UpdateProjectRequest;
import com.beat.taskFlow.project.dto.responses.ProjectResponse;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.project.repository.ProjectRepository;
import com.beat.taskFlow.project.service.ProjectService;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private User otherUser;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = User.builder().name("Proje Sahibi").email("owner@test.com").build();
        owner.setId(1L);

        otherUser = User.builder().name("Diger Kullanici").email("other@test.com").build();
        otherUser.setId(2L);

        project = Project.builder()
                .name("Test Projesi")
                .description("Açıklama")
                .status(ProjectStatus.ACTIVE)
                .owner(owner)
                .members(new HashSet<>())
                .build();
        project.setId(10L);
    }

    @Test
    void createProject_ownerNotAddedToMembersSet() {
        CreateProjectRequest request = new CreateProjectRequest("Yeni Proje", "Açıklama", "#FFFFFF", "backend");

        when(authentication.getName()).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProjectResponse response = projectService.createProject(request, authentication);

        assertNotNull(response);
        assertEquals(0, response.memberCount());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_notOwner_throwsAccessDenied() {
        UpdateProjectRequest request = new UpdateProjectRequest("Guncel Ad", null, null, null, null);

        when(authentication.getName()).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(projectRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(10L, request, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void transferOwnership_oldOwnerBecomesMember() {
        TransferOwnershipRequest request = new TransferOwnershipRequest(2L);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(projectRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.transferOwnership(10L, request, "owner@test.com");

        assertNotNull(response);
        assertEquals(otherUser.getId(), project.getOwner().getId());
        assertTrue(project.getMembers().contains(owner));
    }
}