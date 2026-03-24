package com.alja.reservation.controller.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponseDTO {

    private Long bookingId;
    private String userId;
    private String hotelId;
    private String roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status;
}
