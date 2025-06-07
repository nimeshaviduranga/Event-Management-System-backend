package com.eventmanagement.mapper;

import com.eventmanagement.dto.event.CreateEventRequest;
import com.eventmanagement.dto.event.EventResponse;
import com.eventmanagement.dto.event.UpdateEventRequest;
import com.eventmanagement.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hostId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(CreateEventRequest request);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "hostId", source = "event.hostId")
    @Mapping(target = "hostName", source = "hostName")
    @Mapping(target = "attendeeCount", source = "attendeeCount")
    EventResponse toResponse(Event event, String hostName, long attendeeCount);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hostId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateEventRequest request, @MappingTarget Event event);
}