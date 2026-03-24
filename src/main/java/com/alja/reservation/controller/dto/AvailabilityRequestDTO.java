package com.alja.reservation.controller.dto;

import com.alja.reservation.validation.DateRangeAware;
import com.alja.reservation.validation.ValidDateRange;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ValidDateRange
public class AvailabilityRequestDTO implements DateRangeAware {

    @NotBlank
    private String hotelId;

    @NotNull
    @FutureOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull
    @Future
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

}
