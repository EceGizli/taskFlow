package com.beat.taskFlow.label.controller;

import com.beat.taskFlow.label.dto.requests.CreateLabelRequest;
import com.beat.taskFlow.label.dto.responses.LabelResponse;
import com.beat.taskFlow.label.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Labels", description = "Etiket yönetimi uçları")
@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @Operation(
            summary = "Etiket oluştur",
            description = "Yeni bir etiket oluşturur."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse createLabel(
            @Valid @RequestBody CreateLabelRequest request) {

        return labelService.createLabel(request);
    }

    @Operation(
            summary = "Etiketleri listele",
            description = "Sistemdeki tüm etiketleri listeler."
    )
    @GetMapping
    public List<LabelResponse> getAllLabels() {

        return labelService.getAllLabels();
    }

    @Operation(
            summary = "Etiketi sil",
            description = "Belirtilen etiketi siler."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
    }
}