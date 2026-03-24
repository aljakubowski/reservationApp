package com.alja.reservation.service;

import com.alja.reservation.exception.BookingNotFoundException;
import com.alja.reservation.exception.RoomUnavailableException;
import com.alja.reservation.model.BookingEntity;
import com.alja.reservation.model.BookingStatus;
import com.alja.reservation.repository.BookingRepository;
import com.alja.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public IdempotentCreationResult<BookingEntity> createBookingWithIdempotency(BookingEntity booking, String idempotencyKey) {

        Optional<BookingEntity> existingBooking = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if (existingBooking.isPresent()) {
            log.warn("Idempotency hit! Booking already exists for key: {}", idempotencyKey);
            return new IdempotentCreationResult<>(existingBooking.get(), false);
        }

        log.debug("Locking room {} to prevent concurrent bookings", booking.getRoomId());
        lockRoomEntity(booking);
        validateRoomAvailability(booking);
        booking.setIdempotencyKey(idempotencyKey);
        return saveBooking(booking, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Page<BookingEntity> getUserBookings(String userId, Pageable pageable) {
        return bookingRepository.findAllByUserId(userId, pageable);
    }


    @Transactional(readOnly = true)
    public BookingEntity getByIdAndUserId(Long bookingId, String userId) {
        return findBookingByIdAndUser(bookingId, userId);
    }

    @Transactional
    public BookingEntity cancelBooking(Long bookingId, String userId) {
        BookingEntity booking = getByIdAndUserId(bookingId, userId);

        if (isBookingCancelled(booking)) {
            return booking;
        }

        booking.cancelBooking();
        return bookingRepository.save(booking);
    }

    private void lockRoomEntity(BookingEntity booking) {
        roomRepository.findAndLockById(booking.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room does not exist"));
    }

    private void validateRoomAvailability(BookingEntity booking) {
        boolean isOverlapping = checkRoomAvailability(booking);

        if (isOverlapping) {
            log.warn("Room {} is unavailable for dates {} to {}", booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut());
            throw new RoomUnavailableException("Room " + booking.getRoomId() + " is not available for selected dates.");
        }
    }

    private boolean checkRoomAvailability(BookingEntity booking) {
        return bookingRepository.existsOverlappingBooking(
                booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut());
    }

    private IdempotentCreationResult<BookingEntity> saveBooking(BookingEntity booking, String idempotencyKey) {
        try {
            BookingEntity saved = bookingRepository.saveAndFlush(booking);
            return new IdempotentCreationResult<>(saved, true);
        } catch (DataIntegrityViolationException e) {
            BookingEntity parallelSaved = bookingRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> e);
            return new IdempotentCreationResult<>(parallelSaved, false);
        }
    }

    private BookingEntity findBookingByIdAndUser(Long bookingId, String userId) {
        return bookingRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found or access denied."));
    }


    private boolean isBookingCancelled(BookingEntity booking) {
        return booking.getStatus() == BookingStatus.CANCELLED;
    }

}
