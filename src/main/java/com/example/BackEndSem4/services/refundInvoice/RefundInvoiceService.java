package com.example.BackEndSem4.services.refundInvoice;

import com.example.BackEndSem4.dtos.RefundInvoiceDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Booking;
import com.example.BackEndSem4.models.RefundInvoice;
import com.example.BackEndSem4.repositories.BookingRepository;
import com.example.BackEndSem4.repositories.RefundInvoiceRepository;
import com.example.BackEndSem4.response.refundInvoice.RefundInvoiceListResponse;
import com.example.BackEndSem4.response.refundInvoice.RefundInvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RefundInvoiceService implements IRefundInvoiceService{
    private  final BookingRepository bookingRepository;
    private final RefundInvoiceRepository refundInvoiceRepository;

    @Override
    public RefundInvoiceListResponse getAllRefundInvoices(LocalDate dateRefund, String keyword, String status, Pageable pageable) {
        Page<RefundInvoiceResponse> refundInvoices = refundInvoiceRepository.findAllRefundInvoices(dateRefund, keyword, status, pageable)
                .map(RefundInvoiceResponse::fromRefundInvoiceResponse);

        int totalPages = refundInvoices.getTotalPages();
        List<RefundInvoiceResponse> refundInvoiceResponses = refundInvoices.getContent();

        return RefundInvoiceListResponse.builder()
                .refundInvoiceResponses(refundInvoiceResponses)
                .totalPages(totalPages)
                .build();

    }

    @Override
    public RefundInvoice getRefundInvoiceById(Long id) throws DataNotFoundException {
        return refundInvoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Refund Invoice not found"));
    }

    @Override
    public RefundInvoice getRefundInvoiceByBookingId(Long bookingId) throws DataNotFoundException {
        RefundInvoice refundInvoice = refundInvoiceRepository.findByBooking_Id(bookingId);
        if(refundInvoice == null) {
            throw new DataNotFoundException("Refund Invoice not found");
        }
        return refundInvoice;
    }

    @Transactional
    @Override
    public RefundInvoice createRefundInvoice(RefundInvoiceDTO refundInvoiceDTO) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(refundInvoiceDTO.getBookingId())
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        RefundInvoice refundInvoice = RefundInvoice.builder()
                .booking(booking)
                .bankName(refundInvoiceDTO.getBankName())
                .holderName(refundInvoiceDTO.getHolderName())
                .accountNumber(refundInvoiceDTO.getAccountNumber())
                .status(RefundInvoice.WAITREFUND)
                .build();

        return refundInvoiceRepository.save(refundInvoice);
    }

    @Transactional
    @Override
    public RefundInvoice updateRefundInvoiceStatus(Long id) throws DataNotFoundException {
       RefundInvoice refundInvoice = refundInvoiceRepository.findById(id)
               .orElseThrow(() -> new DataNotFoundException("Refund Invoice not found"));

       refundInvoice.setStatus(RefundInvoice.REFUNDED);
       Booking booking = bookingRepository.findById(refundInvoice.getBooking().getId())
               .orElseThrow(() -> new DataNotFoundException("Booking not found"));
       booking.setStatus(Booking.REFUNDED);
       bookingRepository.save(booking);


        return refundInvoiceRepository.save(refundInvoice);

    }


}
