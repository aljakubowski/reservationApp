package com.alja.reservation.service

import com.alja.reservation.model.BookingEntity
import com.alja.reservation.model.RoomEntity
import com.alja.reservation.repository.BookingRepository
import com.alja.reservation.repository.RoomRepository
import com.alja.reservation.model.BookingStatus
import com.alja.reservation.exception.RoomUnavailableException
import com.alja.reservation.exception.BookingNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class BookingServiceSpec extends Specification {

    @Subject
    private BookingService bookingService

    private BookingRepository bookingRepository = Mock()
    private RoomRepository roomRepository = Mock()

    def setup() {
        bookingService = new BookingService(bookingRepository, roomRepository)
    }

    def "Should successfully create a new booking when room is available (Happy Path)"() {
        given: "A new booking request and an idempotency key"
        def checkIn = LocalDate.now().plusDays(1)
        def checkOut = LocalDate.now().plusDays(3)
        String idempotencyKey = "uuid-1234"
        BookingEntity bookingRequest = new BookingEntity(
                userId: "user-1",
                hotelId: "hotel-a",
                roomId: "room-101",
                checkIn: checkIn,
                checkOut: checkOut
        )

        and: "No existing booking found for this key"
        bookingRepository.findByIdempotencyKey(idempotencyKey) >> Optional.empty()

        and: "Room exists and gets successfully locked"
        roomRepository.findAndLockById("room-101") >> Optional.of(new RoomEntity("room-101", "hotel-a", 0L))

        and: "Room is available (no overlapping bookings)"
        bookingRepository.existsOverlappingBooking("room-101", bookingRequest.checkIn, bookingRequest.checkOut) >> false

        and: "Database successfully saves the new booking"
        BookingEntity savedBooking = new BookingEntity(bookingId: 1L, status: BookingStatus.CONFIRMED)
        bookingRepository.saveAndFlush(_) >> savedBooking

        when: "Creating the booking"
        def result = bookingService.createBookingWithIdempotency(bookingRequest, idempotencyKey)

        then: "Result is a new booking"
        result.isNew()
        result.getEntity().getBookingId() == 1L
    }

    def "Should return existing booking when the same Idempotency-Key is used (Retry)"() {
        given: "An idempotency key and an existing booking in DB"
        String idempotencyKey = "uuid-9999"
        BookingEntity existingBooking = new BookingEntity(bookingId: 5L, status: BookingStatus.CONFIRMED)

        and: "Repository returns the existing booking"
        bookingRepository.findByIdempotencyKey(idempotencyKey) >> Optional.of(existingBooking)

        when: "Client tries to create a booking with the same key"
        def result = bookingService.createBookingWithIdempotency(new BookingEntity(), idempotencyKey)

        then: "Existing booking is returned without executing further logic"
        !result.isNew()
        result.getEntity().getBookingId() == 5L

        and: "No save or lock methods were called"
        0 * roomRepository.findAndLockById(_)
        0 * bookingRepository.saveAndFlush(_)
    }

    def "Should successfully return user bookings (Happy Path)"() {
        given: "User ID and a page request"
        String userId = "user-123"

        BookingEntity booking = new BookingEntity(bookingId: 1L, userId: userId, roomId: "room-101")
        Page<BookingEntity> expectedPage = new PageImpl<>([booking])

        and: "Repository returns page of bookings"
        bookingRepository.findAllByUserId(userId, _ as Pageable) >> expectedPage

        when: "Fetching user bookings"
        def result = bookingService.getUserBookings(userId, _ as Pageable)

        then: "A page containing the bookings is returned"
        result.content.size() == 1
        result.content[0].bookingId == 1L
    }

    def "Should successfully find a booking by booking ID and User ID (Happy Path)"() {
        given: "Booking ID and User ID"
        Long bookingId = 1L
        String userId = "user-123"
        BookingEntity expectedBooking = new BookingEntity(bookingId: bookingId, userId: userId, roomId: "room-101")

        and: "Repository finds the booking"
        bookingRepository.findByBookingIdAndUserId(bookingId, userId) >> Optional.of(expectedBooking)

        when: "Fetching the booking"
        def result = bookingService.getByIdAndUserId(bookingId, userId)

        then: "The correct booking entity is returned"
        result.bookingId == 1L
        result.userId == "user-123"
    }

    def "Should successfully cancel an existing booking (Happy Path)"() {
        given: "An existing confirmed booking"
        Long bookingId = 1L
        String userId = "user-123"
        BookingEntity existingBooking = new BookingEntity(
                bookingId: bookingId,
                userId: userId,
                status: BookingStatus.CONFIRMED
        )

        and: "Repository finds the booking"
        bookingRepository.findByBookingIdAndUserId(bookingId, userId) >> Optional.of(existingBooking)

        and: "Repository saves the cancelled booking"
        BookingEntity savedBooking = new BookingEntity(
                bookingId: bookingId,
                userId: userId,
                status: BookingStatus.CANCELLED
        )
        bookingRepository.save(_ as BookingEntity) >> savedBooking

        when: "Cancelling the booking"
        def result = bookingService.cancelBooking(bookingId, userId)

        then: "The returned booking has CANCELLED status"
        result.status == BookingStatus.CANCELLED
    }

    def "Should throw IllegalArgumentException when trying to book a non-existent room"() {
        given: "A booking request for a non-existent room"
        def bookingRequest = new BookingEntity(roomId: "invalid-room", checkIn: LocalDate.now().plusDays(1), checkOut: LocalDate.now().plusDays(3))
        String idempotencyKey = "key-123"

        and: "No existing idempotency hit"
        bookingRepository.findByIdempotencyKey(idempotencyKey) >> Optional.empty()

        and: "Room repository returns empty (room not found)"
        roomRepository.findAndLockById("invalid-room") >> Optional.empty()

        when: "Trying to create the booking"
        bookingService.createBookingWithIdempotency(bookingRequest, idempotencyKey)

        then: "An exception is thrown from the private lockRoomEntity method"
        def ex = thrown(IllegalArgumentException)
        ex.message == "Room is not available."
    }

    def "Should throw RoomUnavailableException when room has overlapping bookings"() {
        given: "A booking request"
        def bookingRequest = new BookingEntity(roomId: "room-101", checkIn: LocalDate.now().plusDays(1), checkOut: LocalDate.now().plusDays(3))
        String idempotencyKey = "key-456"

        and: "No existing idempotency hit"
        bookingRepository.findByIdempotencyKey(idempotencyKey) >> Optional.empty()

        and: "Room exists"
        roomRepository.findAndLockById("room-101") >> Optional.of(new RoomEntity(roomId: "room-101"))

        and: "But room is NOT available (overlapping exists)"
        bookingRepository.existsOverlappingBooking("room-101", bookingRequest.checkIn, bookingRequest.checkOut) >> true

        when: "Trying to create the booking"
        bookingService.createBookingWithIdempotency(bookingRequest, idempotencyKey)

        then: "An exception is thrown from the private validateRoomAvailability method"
        def ex = thrown(RoomUnavailableException)
        ex.message.contains("is not available for selected dates")
    }

    def "Should throw BookingNotFoundException when trying to cancel someone else's booking"() {
        given: "A booking ID and a wrong User ID"
        Long bookingId = 1L
        String wrongUserId = "hacker-999"

        and: "Repository returns empty because ID and User ID don't match"
        bookingRepository.findByBookingIdAndUserId(bookingId, wrongUserId) >> Optional.empty()

        when: "Trying to cancel the booking"
        bookingService.cancelBooking(bookingId, wrongUserId)

        then: "Exception is thrown from the private findBookingByIdAndUser method"
        thrown(BookingNotFoundException)
    }

    def "Should do nothing and return booking when it is already cancelled"() {
        given: "An already cancelled booking"
        Long bookingId = 1L
        String userId = "user-123"
        def cancelledBooking = new BookingEntity(bookingId: bookingId, userId: userId, status: BookingStatus.CANCELLED)

        and: "Repository finds this booking"
        bookingRepository.findByBookingIdAndUserId(bookingId, userId) >> Optional.of(cancelledBooking)

        when: "Trying to cancel it again"
        def result = bookingService.cancelBooking(bookingId, userId)

        then: "The private isBookingCancelled method stops further logic and repository is NOT called"
        result.status == BookingStatus.CANCELLED
        0 * bookingRepository.save(_)
    }
}
