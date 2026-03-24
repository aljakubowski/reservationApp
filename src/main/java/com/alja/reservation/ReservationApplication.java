package com.alja.reservation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Hotel Reservation API",
                version = "1.0",
                description = "Simple API for checking room availability and managing user bookings. \n\n" +
                        "On application startup, the database is automatically populated with mock data for testing.\n\n" +
                        "The following rooms are created:\n" +
                        "- HOTEL-A (hotel id)\n" +
                        "  - ROOM-101 (room id)\n" +
                        "  - ROOM-102\n" +
                        "  - ROOM-103\n" +
                        "  - ROOM-104\n" +
                        "  - ROOM-105\n" +
                        "- HOTEL-B\n" +
                        "  - ROOM-201\n" +
                        "  - ROOM-202\n" +
                        "  - ROOM-203\n" +
                        "  - ROOM-204\n" +
                        "  - ROOM-205\n\n" +
                        "Additionally, two sample confirmed bookings are inserted:\n" +
                        "- userId: USER-1, hotelId: HOTEL-A, roomId: ROOM-101 (check-in: today + 2 days, check-out: today + 5 days)\n" +
                        "- userId: USER-2, hotelId: HOTEL-A, roomId: ROOM-102 (check-in: today + 10 days, check-out: today + 14 days)\n" +
                "This seed data allows the API to be tested immediately after startup"
        )
)
@SpringBootApplication
public class ReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }
}
