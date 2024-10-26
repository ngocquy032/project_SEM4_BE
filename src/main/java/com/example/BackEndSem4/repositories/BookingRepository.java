package com.example.BackEndSem4.repositories;

import com.example.BackEndSem4.models.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserId(Long userId);

    List<Booking> findAllByUserIdAndStatus(Long userId, String status);

    List<Booking> findAllByScheduleId(Long sheduleId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.user u " +
            "JOIN b.schedule s " +
            "WHERE (:dateBooking IS NULL OR FUNCTION('DATE', b.createdAt) = :dateBooking) " + // Lọc theo ngày
            "AND (:keyword IS NULL OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + // Tìm theo fullName của user
            "LOWER(b.paymentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + // Tìm theo paymentCode
            "LOWER(CAST(s.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(b.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR LOWER(b.status) = LOWER(:status))") // Tìm theo schedule_id
    Page<Booking> findAllBookings(@Param("dateBooking") LocalDate dateBooking,
                                  @Param("keyword") String keyword,
                                  @Param("status") String status,
                                  Pageable pageable);


    @Query("SELECT b FROM Booking b " +
            "JOIN b.user u " +
            "JOIN b.schedule s " +
            "WHERE (:dateBooking IS NULL OR FUNCTION('DATE', b.createdAt) = :dateBooking) " + // Lọc theo ngày đặt
            "AND (:keyword IS NULL OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + // Tìm theo tên đầy đủ của người dùng
            "LOWER(CAST(b.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + // Tìm theo booking id
            "AND (:status IS NULL OR :status = '' OR LOWER(b.status) = LOWER(:status)) " + // Tìm theo trạng thái
            "AND (:scheduleId IS NULL OR s.id = :scheduleId)") // Lọc theo scheduleId
    Page<Booking> findAllBookingsDoctor(@Param("dateBooking") LocalDate dateBooking,
                                  @Param("keyword") String keyword,
                                  @Param("status") String status,
                                  @Param("scheduleId") Long scheduleId, // Tham số mới
                                  Pageable pageable);



    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :sevenDaysAgo AND b.createdAt <= :now AND b.status = 'paid'")
    List<Booking> findBookingsInLast7Days(LocalDateTime sevenDaysAgo, LocalDateTime now);


}
