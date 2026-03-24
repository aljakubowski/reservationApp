package com.alja.reservation.mapper

import com.alja.reservation.model.RoomEntity
import spock.lang.Specification

class AvailabilityMapperTest extends Specification {

    private AvailabilityMapper mapper = new AvailabilityMapperImpl()

    def "Should successfully map list of RoomEntities to DTO (Happy Path)"() {
        given: "A list of room entities"
        def rooms =[
                new RoomEntity(roomId: "ROOM-1", hotelId: "HOTEL"),
                new RoomEntity(roomId: "ROOM-2", hotelId: "HOTEL")
        ]

        when: "Mapping to DTO"
        def result = mapper.toDto(rooms)

        then: "DTO contains exactly mapped room IDs"
        result.roomIds.size() == 2
        result.roomIds.containsAll(["ROOM-1", "ROOM-2"])
    }
}
