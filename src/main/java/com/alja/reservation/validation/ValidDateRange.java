package com.alja.reservation.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    String message() default "Check-out date must be after check-in date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
