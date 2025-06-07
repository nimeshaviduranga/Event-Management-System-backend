package com.eventmanagement.dto.event;

import com.eventmanagement.entity.Visibility;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateEventRequest {

    @Size(max = 100, message = "Title shouldn't exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description shoundn't exceed 500 characters")
    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Size(max = 500, message = "Location shouldnt exceed 500 characters")
    private String location;

    private Visibility visibility;
}