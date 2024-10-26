package com.example.BackEndSem4.services.refundInvoice;

import com.example.BackEndSem4.dtos.RefundInvoiceDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.RefundInvoice;
import com.example.BackEndSem4.response.refundInvoice.RefundInvoiceListResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IRefundInvoiceService {
    RefundInvoiceListResponse getAllRefundInvoices(LocalDate dateBooking, String keyword, String status, Pageable pageable);

    RefundInvoice getRefundInvoiceById(Long id) throws DataNotFoundException;

    RefundInvoice getRefundInvoiceByBookingId(Long bookingId) throws DataNotFoundException;


    RefundInvoice createRefundInvoice(RefundInvoiceDTO refundInvoiceDTO) throws DataNotFoundException;

    RefundInvoice updateRefundInvoiceStatus(Long id) throws DataNotFoundException;

}
