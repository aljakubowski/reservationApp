package com.alja.reservation.controller

import com.alja.reservation.model.RoomEntity
import com.alja.reservation.repository.BookingRepository
import com.alja.reservation.repository.RoomRepository
import com.alja.reservation.model.BookingEntity
import com.alja.reservation.model.BookingStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Stepwise
class BookingControllerITSpec extends Specification {

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
        if (roomRepository.count() == 0) {
            roomRepository.save(new RoomEntity("ROOM-TEST-1", "HOTEL-TEST", 0L))
        }
    }

    def "POST /api/v1/bookings - Should create a booking and return 201 Created (Happy Path)"() {
        given: "A valid booking JSON payload"
        String idempotencyKey = "integration-test-uuid-1"
        def checkIn = LocalDate.now().plusDays(1)
        def checkOut = LocalDate.now().plusDays(3)

        String jsonPayload = """
        {
            "userId": "user-123",
            "hotelId": "HOTEL-TEST",
            "roomId": "ROOM-TEST-1",
            "checkIn": "${checkIn}",
            "checkOut": "${checkOut}"
        }
        """

        expect: "Sending POST request creates the booking"
        mockMvc.perform(post("/api/v1/bookings")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.bookingId").exists())
                .andExpect(jsonPath("\$.status").value("CONFIRMED"))
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
    }

    def "GET /api/v1/bookings - Should return a paginated list of user's bookings (Happy Path)"() {
        given: "A booking exists in the database for a specific user"
        def userId = "user-page-test"
        def booking = BookingEntity.builder()
                .userId(userId)
                .hotelId("HOTEL-TEST")
                .roomId("ROOM-TEST-1")
                .checkIn(LocalDate.now().plusDays(5))
                .checkOut(LocalDate.now().plusDays(10))
                .status(BookingStatus.CONFIRMED)
                .idempotencyKey("idempotency-key-2")
                .build()
        bookingRepository.save(booking)

        expect: "Fetching bookings returns 200 OK and a Page structure containing the booking"
        mockMvc.perform(get("/api/v1/bookings")
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.content').isArray())
                .andExpect(jsonPath('$.content.length()').value(1))
                .andExpect(jsonPath('$.content[0].userId').value(userId))
                .andExpect(jsonPath('$.content[0].roomId').value("ROOM-TEST-1"))
                .andExpect(jsonPath('$.content[0].status').value("CONFIRMED"))
                .andExpect(jsonPath('$.totalElements').value(1))
    }

    def "GET /api/v1/bookings/{bookingId} - Should return specific booking details (Happy Path)"() {
        given: "A booking exists in the database"
        def userId = "user-single-test"
        def booking = BookingEntity.builder()
                .userId(userId)
                .hotelId("HOTEL-TEST")
                .roomId("ROOM-TEST-1")
                .checkIn(LocalDate.now().plusDays(15))
                .checkOut(LocalDate.now().plusDays(20))
                .status(BookingStatus.CONFIRMED)
                .idempotencyKey("idempotency-key-3")
                .build()
        def savedBooking = bookingRepository.save(booking)

        expect: "Fetching by ID returns 200 OK with correct booking data"
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", savedBooking.getBookingId())
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.bookingId').value(savedBooking.getBookingId()))
                .andExpect(jsonPath('$.userId').value(userId))
                .andExpect(jsonPath('$.roomId').value("ROOM-TEST-1"))
                .andExpect(jsonPath('$.status').value("CONFIRMED"))
    }

    def "PATCH /api/v1/bookings/{bookingId}/cancel - Should cancel the booking successfully (Happy Path)"() {
        given: "A confirmed booking exists in the database"
        def userId = "user-cancel-test"
        def booking = BookingEntity.builder()
                .userId(userId)
                .hotelId("HOTEL-TEST")
                .roomId("ROOM-TEST-1")
                .checkIn(LocalDate.now().plusDays(25))
                .checkOut(LocalDate.now().plusDays(30))
                .status(BookingStatus.CONFIRMED)
                .idempotencyKey("idempotency-key-4")
                .build()
        def savedBooking = bookingRepository.save(booking)

        expect: "PATCH request returns 200 OK and status in response is CANCELLED"
        mockMvc.perform(patch("/api/v1/bookings/{bookingId}/cancel", savedBooking.getBookingId())
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.bookingId').value(savedBooking.getBookingId()))
                .andExpect(jsonPath('$.status').value("CANCELLED"))

        and: "The status is actually updated to CANCELLED in the database"
        def updatedBooking = bookingRepository.findById(savedBooking.getBookingId()).orElseThrow()
        updatedBooking.getStatus() == BookingStatus.CANCELLED
    }


}
