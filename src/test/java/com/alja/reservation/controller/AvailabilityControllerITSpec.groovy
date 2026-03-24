package com.alja.reservation.controller

import com.alja.reservation.model.RoomEntity
import com.alja.reservation.repository.BookingRepository
import com.alja.reservation.repository.RoomRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Stepwise
class AvailabilityControllerITSpec extends Specification {

    @Shared
    static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")

    static {
        postgres.start()
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop")
    }

    @Autowired
    MockMvc mockMvc

    @Autowired
    RoomRepository roomRepository

    @Autowired
    BookingRepository bookingRepository;

    def setup() {
        bookingRepository.deleteAll()
        roomRepository.deleteAll()
        roomRepository.saveAll([
                new RoomEntity("ROOM-101", "HOTEL-A", 0L),
                new RoomEntity("ROOM-102", "HOTEL-A", 0L)
        ])
    }

    def "GET /api/v1/availability - Should return available rooms (Happy Path)"() {
        given: "Valid search criteria"
        def hotelId = "HOTEL-A"
        def checkIn = LocalDate.now().plusDays(1).toString()
        def checkOut = LocalDate.now().plusDays(5).toString()

        expect: "Endpoint returns 200 OK and both available rooms"
        mockMvc.perform(get("/api/v1/availability")
                .param("hotelId", hotelId)
                .param("checkIn", checkIn)
                .param("checkOut", checkOut))
                .andExpect(status().isOk())
                .andExpect(jsonPath("\$.roomIds").isArray())
                .andExpect(jsonPath("\$.roomIds.length()").value(2))
                .andExpect(jsonPath("\$.roomIds[0]").value("ROOM-101"))
                .andExpect(jsonPath("\$.roomIds[1]").value("ROOM-102"))
    }
}