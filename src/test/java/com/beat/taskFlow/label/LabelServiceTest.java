package com.beat.taskFlow.label;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.label.dto.requests.CreateLabelRequest;
import com.beat.taskFlow.label.dto.responses.LabelResponse;
import com.beat.taskFlow.label.entity.Label;
import com.beat.taskFlow.label.repository.LabelRepository;
import com.beat.taskFlow.label.service.LabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private LabelService labelService;

    private Label label;

    @BeforeEach
    void setUp() {
        label = Label.builder()
                .name("bug")
                .color("#FF0000")
                .build();
    }

    @Test
    void createLabel_shouldSucceed_withValidRequest() {
        CreateLabelRequest request = new CreateLabelRequest("bug", "#FF0000");

        when(labelRepository.existsByNameIgnoreCase("bug")).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(label);

        LabelResponse response = labelService.createLabel(request);

        assertThat(response.name()).isEqualTo("bug");
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    void createLabel_shouldThrowException_whenNameAlreadyExists() {
        CreateLabelRequest request = new CreateLabelRequest("bug", "#FF0000");

        when(labelRepository.existsByNameIgnoreCase("bug")).thenReturn(true);

        assertThatThrownBy(() -> labelService.createLabel(request))
                .isInstanceOf(AlreadyExistsException.class);

        verify(labelRepository, never()).save(any());
    }

    @Test
    void deleteLabel_shouldThrowException_whenLabelNotFound() {
        when(labelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.deleteLabel(99L))
                .isInstanceOf(NotFoundException.class);
    }
}