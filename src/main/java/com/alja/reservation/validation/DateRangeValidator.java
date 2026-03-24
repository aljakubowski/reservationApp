package com.alja.reservation.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeAware> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
    }

    @Override
    public boolean isValid(DateRangeAware value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate checkIn = value.getCheckIn();
        LocalDate checkOut = value.getCheckOut();

        if (checkIn == null || checkOut == null) {
            return true;
        }

        return checkIn.isBefore(checkOut);
    }
}
