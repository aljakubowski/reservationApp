package com.alja.reservation.mapper;

import com.alja.reservation.controller.dto.BookingRequestDTO;
import com.alja.reservation.controller.dto.BookingResponseDTO;
import com.alja.reservation.model.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    BookingResponseDTO toDto(BookingEntity booking);

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    BookingEntity toEntity(BookingRequestDTO dto);
}
