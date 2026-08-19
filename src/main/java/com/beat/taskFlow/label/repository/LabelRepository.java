package com.beat.taskFlow.label.repository;

import com.beat.taskFlow.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
	boolean existsByNameIgnoreCase(String name);
}