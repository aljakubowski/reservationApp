package com.alja.reservation.controller;

import com.alja.reservation.controller.dto.AvailabilityRequestDTO;
import com.alja.reservation.controller.dto.AvailabilityResponseDTO;
import com.alja.reservation.mapper.AvailabilityMapper;
import com.alja.reservation.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Availability", description = "Endpoints for checking hotel and room availability")
@RestController
@RequestMapping(AvailabilityController.AVAILABILITY_API_V1)
@RequiredArgsConstructor
public class AvailabilityController {

    public static final String AVAILABILITY_API_V1 = "/api/v1/availability";

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    @Operation(
            summary = "Get available rooms",
            description = "Returns a list of rooms available in the given date range for a specific hotel."
    )
    @GetMapping()
    public ResponseEntity<AvailabilityResponseDTO> checkAvailability(
            @Parameter(description = "Criteria for searching available rooms (hotel ID, check-in, check-out dates)")
            @Valid @ModelAttribute AvailabilityRequestDTO availabilityRequestDTO) {

        var availableRooms = availabilityService.getAvailableRooms(
                availabilityRequestDTO.getHotelId(),
                availabilityRequestDTO.getCheckIn(),
                availabilityRequestDTO.getCheckOut());

        return ResponseEntity.ok(availabilityMapper.toDto(availableRooms));
    }
}
