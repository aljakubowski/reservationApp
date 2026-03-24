package com.alja.reservation.repository;

import com.alja.reservation.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, String> {

    @Query("SELECT r FROM RoomEntity r WHERE r.hotelId = :hotelId AND r.roomId NOT IN (" +
            "SELECT b.roomId FROM BookingEntity b WHERE b.hotelId = :hotelId " +
            "AND b.status != 'CANCELLED' " +
            "AND (b.checkIn < :checkOut AND b.checkOut > :checkIn)" +
            ")")
    List<RoomEntity> findAvailableRooms(
            @Param("hotelId") String hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT r FROM RoomEntity r WHERE r.roomId = :roomId")
    Optional<RoomEntity> findAndLockById(@Param("roomId") String roomId);
}