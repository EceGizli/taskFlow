package com.beat.taskFlow.task.entity.concretes;

import com.beat.taskFlow.label.entity.Label;
import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import com.beat.taskFlow.project.entity.concretes.Project;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import com.beat.taskFlow.user.entity.concretes.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    private LocalDate dueDate;

    private Integer estimatedHours;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @ManyToMany
    @JoinTable(
            name = "task_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @Builder.Default
    private Set<Label> labels = new HashSet<>();
}