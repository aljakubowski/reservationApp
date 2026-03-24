package com.alja.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user", columnList = "userId"),
        @Index(name = "idx_booking_room_dates", columnList = "roomId, status, checkIn, checkOut"),
        @Index(name = "idx_booking_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String hotelId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.CONFIRMED;
        }
    }

    public void cancelBooking(){
        this.status = BookingStatus.CANCELLED;
    }

}
