package com.beat.taskFlow.label.entity;

import com.beat.taskFlow.project.entity.abstracts.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "labels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 7)
    private String color;
}