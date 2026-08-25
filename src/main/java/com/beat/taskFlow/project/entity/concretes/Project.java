package com.beat.taskFlow.project.entity.concretes;

import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import com.beat.taskFlow.user.entity.concretes.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(length = 7)
    private String color;

    private String tag;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();
}