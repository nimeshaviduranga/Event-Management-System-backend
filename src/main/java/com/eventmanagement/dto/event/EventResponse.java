package com.eventmanagement.dto.event;

import com.eventmanagement.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;
    private String title;
    private String description;
    private String hostId;
    private String hostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Visibility visibility;
    private long attendeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}