package com.beat.taskFlow.label.service;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.label.dto.requests.CreateLabelRequest;
import com.beat.taskFlow.label.dto.responses.LabelResponse;
import com.beat.taskFlow.label.entity.Label;
import com.beat.taskFlow.label.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    @Transactional
    public LabelResponse createLabel(CreateLabelRequest request) {
        if (labelRepository.existsByNameIgnoreCase(request.name())) {
            throw new AlreadyExistsException("Bu isimde bir etiket zaten mevcut: " + request.name());
        }
        Label label = Label.builder()
                .name(request.name())
                .color(request.color())
                .build();
        return mapToResponse(labelRepository.save(label));
    }

    @Transactional
    public void deleteLabel(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etiket bulunamadı: id=" + id));
        labelRepository.delete(label);
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getAllLabels() {

        return labelRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LabelResponse mapToResponse(Label label) {

        return new LabelResponse(
                label.getId(),
                label.getName(),
                label.getColor(),
                label.getCreatedAt(),
                label.getUpdatedAt()
        );
    }
}