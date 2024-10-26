package com.example.BackEndSem4.repositories;


import com.example.BackEndSem4.models.RefundInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RefundInvoiceRepository extends JpaRepository<RefundInvoice, Long> {

    RefundInvoice findByBooking_Id(Long bookingId);



    @Query("SELECT r FROM RefundInvoice r " +
            "WHERE (:dateRefund IS NULL OR :dateRefund = '' OR FUNCTION('DATE', r.createdAt) = :dateRefund) " +
            "AND (:keyword IS NULL OR :keyword = '' OR CAST(r.booking.id AS string) LIKE %:keyword%) " +
            "AND (:status IS NULL OR :status = '' OR r.status = :status)")
    Page<RefundInvoice> findAllRefundInvoices(
            @Param("dateRefund") LocalDate dateRefund,
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable);
}
