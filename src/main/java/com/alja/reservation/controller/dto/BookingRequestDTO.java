package com.alja.reservation.controller.dto;

import com.alja.reservation.validation.DateRangeAware;
import com.alja.reservation.validation.ValidDateRange;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ValidDateRange
public class BookingRequestDTO implements DateRangeAware {

    @NotBlank
    private String userId;

    @NotBlank
    private String hotelId;

    @NotBlank
    private String roomId;

    @NotNull
    @FutureOrPresent
    private LocalDate checkIn;

    @NotNull
    @Future
    private LocalDate checkOut;

}
