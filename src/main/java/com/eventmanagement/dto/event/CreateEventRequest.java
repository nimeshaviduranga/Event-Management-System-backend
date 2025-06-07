package com.eventmanagement.dto.event;

import com.eventmanagement.entity.Visibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateEventRequest {

    @NotBlank
    @Size(max = 100, message = "Should not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description should not exceed 500 characters")
    private String description;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotBlank
    @Size(max = 500, message = "Location should not exceed 500 characters")
    private String location;

    private Visibility visibility = Visibility.PUBLIC;
}