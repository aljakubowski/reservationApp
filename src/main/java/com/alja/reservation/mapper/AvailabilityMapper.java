package com.alja.reservation.mapper;

import com.alja.reservation.controller.dto.AvailabilityResponseDTO;
import com.alja.reservation.model.RoomEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvailabilityMapper {

    default AvailabilityResponseDTO toDto(List<RoomEntity> roomEntities) {
        AvailabilityResponseDTO dto = new AvailabilityResponseDTO();
        if (roomEntities == null || roomEntities.isEmpty()) {
            dto.setRoomIds(Collections.emptyList());
            return dto;
        }
        List<String> ids = roomEntities.stream()
                .map(RoomEntity::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        dto.setRoomIds(ids);
        return dto;
    }

}
