package com.beat.taskFlow.project;

import com.beat.taskFlow.project.dto.requests.CreateProjectRequest;
import com.beat.taskFlow.project.dto.requests.TransferOwnershipRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private ProjectService projectService;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        owner = User.builder().email("owner@test.com").name("Owner").build();
        owner.setId(1L);

        other = User.builder().email("other@test.com").name("Other").build();
        other.setId(2L);
    }

    @Test
    void createProject_ownerNotAddedToMembersSet() {
        when(authentication.getName()).thenReturn(owner.getEmail());
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProjectRequest req = new CreateProjectRequest("Proje", "desc", "#fff", "tag");
        ProjectResponse response = projectService.createProject(req, authentication);

        assertThat(response.memberCount()).isEqualTo(1); // sadece owner
    }

    @Test
    void updateProject_notOwner_throwsAccessDenied() {
        Project project = Project.builder().owner(owner).status(ProjectStatus.ACTIVE).build();
        project.setId(10L);

        when(authentication.getName()).thenReturn(other.getEmail());
        when(userRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(10L, null, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void transferOwnership_oldOwnerBecomesMember() {
        Project project = Project.builder().owner(owner).status(ProjectStatus.ACTIVE).build();
        project.setId(10L);

        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(userRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        projectService.transferOwnership(10L, new TransferOwnershipRequest(other.getId()), owner.getEmail());

        assertThat(project.getOwner()).isEqualTo(other);
        assertThat(project.getMembers()).contains(owner);
    }
}