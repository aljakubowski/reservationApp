package com.alja.reservation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdempotentCreationResult<T> {
    private final T entity;
    private final boolean isNew;
}
