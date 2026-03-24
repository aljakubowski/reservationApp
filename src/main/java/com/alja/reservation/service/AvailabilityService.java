package com.alja.reservation.service;

import com.alja.reservation.model.RoomEntity;
import com.alja.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<RoomEntity> getAvailableRooms(String hotelId,
                                              LocalDate checkIn,
                                              LocalDate checkOut) {
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut);
    }
}
