package com.beat.taskFlow.project;

import com.beat.taskFlow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project extends BaseEntity {

    @NotBlank(message = "Proje adı boş bırakılamaz")
    @Size(max = 150, message = "Proje adı en fazla 150 karakter olabilir")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}