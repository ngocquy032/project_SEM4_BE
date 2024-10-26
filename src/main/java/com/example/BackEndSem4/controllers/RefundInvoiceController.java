package com.example.BackEndSem4.controllers;

import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.RefundInvoice;
import com.example.BackEndSem4.response.Response;
import com.example.BackEndSem4.response.refundInvoice.RefundInvoiceListResponse;
import com.example.BackEndSem4.response.refundInvoice.RefundInvoiceResponse;
import com.example.BackEndSem4.services.refundInvoice.RefundInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("${api.prefix}/refundInvoices")
@RequiredArgsConstructor
public class RefundInvoiceController {

    private final RefundInvoiceService refundInvoiceService;



    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllRefundInvoices( @RequestParam(defaultValue = "") LocalDate dateRefund,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "999999999") int limit,
                                                   @RequestParam(defaultValue = "") String sort,
                                                   @RequestParam(defaultValue = "") String keyword,
                                                   @RequestParam(defaultValue = "") String status
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by(sortDirection,"id")
        );
        RefundInvoiceListResponse refundInvoiceListResponse = refundInvoiceService.getAllRefundInvoices(dateRefund, keyword, status, pageRequest);
        return ResponseEntity.ok(new Response("success", "Booking payment successfully.", refundInvoiceListResponse));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRefundInvoiceById(@PathVariable Long id) throws DataNotFoundException {
        RefundInvoice refundInvoice = refundInvoiceService.getRefundInvoiceById(id);
        return ResponseEntity.ok(new Response("success", "Booking payment successfully.", RefundInvoiceResponse.fromRefundInvoiceResponse(refundInvoice)));

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getRefundInvoiceByBookingId(@PathVariable Long bookingId) throws DataNotFoundException {
        RefundInvoice refundInvoice = refundInvoiceService.getRefundInvoiceByBookingId(bookingId);
        return ResponseEntity.ok(new Response("success", "Booking payment successfully.", RefundInvoiceResponse.fromRefundInvoiceResponse(refundInvoice)));

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/refunded/{id}")
    public ResponseEntity<?> updateRefundInvoice(
            @PathVariable Long id) throws DataNotFoundException {
            RefundInvoice updatedRefundInvoice = refundInvoiceService.updateRefundInvoiceStatus(id);
        return ResponseEntity.ok(new Response("success", "Refunded successfully.", null));
    }
}
