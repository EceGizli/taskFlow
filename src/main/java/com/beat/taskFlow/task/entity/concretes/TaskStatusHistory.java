package com.beat.taskFlow.task.entity.concretes;

import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;
}