package com.alja.reservation.validation;

import java.time.LocalDate;

public interface DateRangeAware {
    LocalDate getCheckIn();
    LocalDate getCheckOut();
}
