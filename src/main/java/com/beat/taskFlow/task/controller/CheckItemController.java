package com.beat.taskFlow.task.controller;

import com.beat.taskFlow.task.dto.requests.CreateCheckItemRequest;
import com.beat.taskFlow.task.dto.requests.UpdateCheckItemRequest;
import com.beat.taskFlow.task.dto.responses.CheckItemResponse;
import com.beat.taskFlow.task.service.CheckItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Checklist", description = "Görev kontrol listesi (CheckItem) yönetim uçları")
public class CheckItemController {

    private final CheckItemService checkItemService;

    @Operation(summary = "Göreve kontrol maddesi ekle", description = "Belirtilen göreve yeni bir checklist maddesi ekler.")
    @PostMapping("/tasks/{taskId}/check-items")
    public ResponseEntity<CheckItemResponse> createCheckItem(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCheckItemRequest request,
            Authentication authentication) {
        CheckItemResponse created = checkItemService.createCheckItem(taskId, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Görevin kontrol listesini getir", description = "Belirtilen göreve ait tüm checklist maddelerini listeler.")
    @GetMapping("/tasks/{taskId}/check-items")
    public ResponseEntity<List<CheckItemResponse>> getCheckItems(
            @PathVariable Long taskId,
            Authentication authentication) {
        return ResponseEntity.ok(checkItemService.getCheckItemsByTaskId(taskId, authentication));
    }

    @Operation(summary = "Kontrol maddesini güncelle", description = "Maddenin başlığını veya tamamlanma durumunu günceller.")
    @PutMapping("/check-items/{checkItemId}")
    public ResponseEntity<CheckItemResponse> updateCheckItem(
            @PathVariable Long checkItemId,
            @Valid @RequestBody UpdateCheckItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(checkItemService.updateCheckItem(checkItemId, request, authentication));
    }

    @Operation(summary = "Kontrol maddesi durumunu tersine çevir (Toggle)", description = "Maddenin tamamlandı/tamamlanmadı durumunu hızlıca değiştirir.")
    @PatchMapping("/check-items/{checkItemId}/toggle")
    public ResponseEntity<CheckItemResponse> toggleCheckItem(
            @PathVariable Long checkItemId,
            Authentication authentication) {
        return ResponseEntity.ok(checkItemService.toggleCheckItem(checkItemId, authentication));
    }

    @Operation(summary = "Kontrol maddesini sil", description = "Belirtilen checklist maddesini sistemden siler.")
    @DeleteMapping("/check-items/{checkItemId}")
    public ResponseEntity<Void> deleteCheckItem(
            @PathVariable Long checkItemId,
            Authentication authentication) {
        checkItemService.deleteCheckItem(checkItemId, authentication);
        return ResponseEntity.noContent().build();
    }
}