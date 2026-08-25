package com.beat.taskFlow.task.entity.concretes;

import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "check_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckItem extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private Task task;
}