package com.alja.reservation.controller;

import com.alja.reservation.controller.dto.BookingRequestDTO;
import com.alja.reservation.controller.dto.BookingResponseDTO;
import com.alja.reservation.mapper.BookingMapper;
import com.alja.reservation.model.BookingEntity;
import com.alja.reservation.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;

@Tag(name = "Bookings", description = "Endpoints for managing user bookings")
@RestController
@RequestMapping(BookingController.BOOKINGS_API_V1)
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {

    public static final String BOOKINGS_API_V1 = "/api/v1/bookings";

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    public static final String BOOKING_ID = "/{bookingId}";
    public static final String CANCEL = "/cancel";

    private final BookingMapper bookingMapper;
    private final BookingService bookingService;

    @Operation(
            summary = "Create a new booking",
            description = "Creates a hotel room booking. Uses the Idempotency-Key header to safely retry requests without creating duplicate bookings."
    )
    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(
            @Parameter(description = "Unique key to ensure idempotent request processing", example = "uuid-1234")
            @RequestHeader(IDEMPOTENCY_HEADER) @NotBlank String idempotencyKey,
            @Parameter(description = "Booking details including room, hotel, dates, and user ID")
            @Valid @RequestBody BookingRequestDTO bookingRequest) {

        log.info("Received booking creation request for user: {}, room: {}", bookingRequest.getUserId(), bookingRequest.getRoomId());

        var entityToSave = bookingMapper.toEntity(bookingRequest);
        var result = bookingService.createBookingWithIdempotency(entityToSave, idempotencyKey);
        var dto = bookingMapper.toDto(result.getEntity());

        if (!result.isNew()) {
            return ResponseEntity.ok(dto);
        }

        log.info("Successfully created new booking with ID: {}", dto.getBookingId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path(BOOKING_ID)
                .buildAndExpand(dto.getBookingId())
                .toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @Operation(
            summary = "Get user bookings",
            description = "Returns a paginated list of all active and cancelled bookings for a specific user."
    )
    @GetMapping
    public ResponseEntity<Page<BookingResponseDTO>> getUserBookings(
            @Parameter(description = "ID of the user whose bookings are being fetched")
            @RequestParam @NotBlank String userId,
            @Parameter(description = "Pagination parameters (page number, size, sorting)")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<BookingEntity> userBookingsPage = bookingService.getUserBookings(userId, pageable);
        Page<BookingResponseDTO> responsePage = userBookingsPage.map(bookingMapper::toDto);

        return ResponseEntity.ok(responsePage);
    }

    @Operation(
            summary = "Get booking details",
            description = "Returns details of a specific booking by its ID. Requires user ID to ensure data privacy."
    )
    @GetMapping(BOOKING_ID)
    public ResponseEntity<BookingResponseDTO> getBookingByBookingId(@Parameter(description = "Unique ID of the booking")
                                                                    @PathVariable Long bookingId,
                                                                    @Parameter(description = "ID of the user who owns the booking")
                                                                    @RequestParam @NotBlank String userId) {
        var userBooking = bookingService.getByIdAndUserId(bookingId, userId);
        var bookingResponseDto = bookingMapper.toDto(userBooking);

        return ResponseEntity.ok(bookingResponseDto);
    }

    @Operation(
            summary = "Cancel a booking",
            description = "Cancels an existing booking. The room becomes available for other users."
    )
    @PatchMapping(BOOKING_ID + CANCEL)
    public ResponseEntity<BookingResponseDTO> cancelBooking(@Parameter(description = "Unique ID of the booking to cancel")
                                                            @PathVariable Long bookingId,
                                                            @Parameter(description = "ID of the user who owns the booking")
                                                            @RequestParam @NotBlank String userId) {

        log.info("Received cancellation request for booking ID: {} by user: {}", bookingId, userId);

        var cancelledBooking = bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(bookingMapper.toDto(cancelledBooking));
    }
}