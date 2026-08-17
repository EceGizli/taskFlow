package com.beat.taskFlow.project.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
	    @NotBlank(message = "Proje ismi boş bırakılamaz")
	    @Size(max = 150, message = "Proje ismi en fazla 150 karakter olabilir")
	    String name,

	    String description,

	    @Size(max = 20, message = "Renk en fazla 20 karakter olabilir")
	    String color,

	    @Size(max = 50, message = "Etiket en fazla 50 karakter olabilir")
	    String tag
	) {}