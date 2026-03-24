package com.alja.reservation.repository;

import com.alja.reservation.model.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    Page<BookingEntity> findAllByUserId(String userId, Pageable pageable);

    Optional<BookingEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COUNT(b) > 0 FROM BookingEntity b " +
            "WHERE b.roomId = :roomId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkIn < :checkOut " +
            "AND b.checkOut > :checkIn")
    boolean existsOverlappingBooking(
            @Param("roomId") String roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    Optional<BookingEntity> findByBookingIdAndUserId(Long bookingId, String userId);
}
