package com.alja.reservation.config;

import com.alja.reservation.model.BookingEntity;
import com.alja.reservation.model.BookingStatus;
import com.alja.reservation.model.RoomEntity;
import com.alja.reservation.repository.BookingRepository;
import com.alja.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 *  auto database population with mock rooms (e.g., ROOM-101 in HOTEL-A) on startup for testing purpose.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void run(String... args) {

        if (roomRepository.count() == 0) {
            log.info("Populating database with mock data...");

            List<RoomEntity> rooms = List.of(
                    new RoomEntity("ROOM-101", "HOTEL-A", 0L),
                    new RoomEntity("ROOM-102", "HOTEL-A", 0L),
                    new RoomEntity("ROOM-103", "HOTEL-A", 0L),
                    new RoomEntity("ROOM-104", "HOTEL-A", 0L),
                    new RoomEntity("ROOM-105", "HOTEL-A", 0L),
                    new RoomEntity("ROOM-201", "HOTEL-B", 0L),
                    new RoomEntity("ROOM-202", "HOTEL-B", 0L),
                    new RoomEntity("ROOM-203", "HOTEL-B", 0L),
                    new RoomEntity("ROOM-204", "HOTEL-B", 0L),
                    new RoomEntity("ROOM-205", "HOTEL-B", 0L)
            );
            roomRepository.saveAll(rooms);

            List<BookingEntity> bookings = List.of(
                    BookingEntity.builder()
                            .userId("USER-1")
                            .hotelId("HOTEL-A")
                            .roomId("ROOM-101")
                            .checkIn(LocalDate.now().plusDays(2))
                            .checkOut(LocalDate.now().plusDays(5))
                            .status(BookingStatus.CONFIRMED)
                            .idempotencyKey(UUID.randomUUID().toString())
                            .build(),
                    BookingEntity.builder()
                            .userId("USER-2")
                            .hotelId("HOTEL-A")
                            .roomId("ROOM-102")
                            .checkIn(LocalDate.now().plusDays(10))
                            .checkOut(LocalDate.now().plusDays(14))
                            .status(BookingStatus.CONFIRMED)
                            .idempotencyKey(UUID.randomUUID().toString())
                            .build()
            );
            bookingRepository.saveAll(bookings);

            log.info("Database populated successfully!");
        }
    }
}
