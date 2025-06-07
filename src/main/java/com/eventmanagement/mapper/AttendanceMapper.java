package com.eventmanagement.mapper;

import com.eventmanagement.dto.attendance.AttendanceResponse;
import com.eventmanagement.dto.attendance.CreateAttendanceRequest;
import com.eventmanagement.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    Attendance toEntity(CreateAttendanceRequest request);

    @Mapping(target = "id", source = "attendance.id")
    @Mapping(target = "eventId", source = "attendance.eventId")
    @Mapping(target = "userId", source = "attendance.userId")
    @Mapping(target = "status", source = "attendance.status")
    @Mapping(target = "respondedAt", source = "attendance.respondedAt")
    @Mapping(target = "eventTitle", source = "eventTitle")
    @Mapping(target = "userName", source = "userName")
    AttendanceResponse toResponse(Attendance attendance, String eventTitle, String userName);
}