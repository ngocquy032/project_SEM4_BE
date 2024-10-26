package com.example.BackEndSem4.controllers;

import com.example.BackEndSem4.dtos.BookingDTO;
import com.example.BackEndSem4.dtos.EmailBookingDTO;
import com.example.BackEndSem4.dtos.RefundInvoiceDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Booking;

import com.example.BackEndSem4.models.Email;
import com.example.BackEndSem4.response.Response;

import com.example.BackEndSem4.response.booking.BookingListResponse;
import com.example.BackEndSem4.response.booking.BookingResponse;
import com.example.BackEndSem4.services.booking.BookingService;
import com.example.BackEndSem4.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/bookings")
@RequiredArgsConstructor
public class BookingController {


    private  final BookingService bookingService;
    private final EmailService emailService;


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("")
    public ResponseEntity<?> createBooking(@RequestBody BookingDTO bookingDTO) throws DataNotFoundException {
        Booking booking = bookingService.createBooking(bookingDTO);
        // Send email
        Email email = Email.builder()
                .toEmail(booking.getUser().getEmail())
                .subject("Appointment Confirmation")
                .emailBookingDTO(EmailBookingDTO.fromBookingData(booking))
                .emailMedicalResultDTO(null)
                .build();
        emailService.sendEmail(email);
        return ResponseEntity.ok(new Response("success", "Create booking successfully.", BookingResponse.fromBookingResponse(booking)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody BookingDTO bookingDTO) throws DataNotFoundException {
        Booking booking = bookingService.updateBooking(id, bookingDTO);
        return ResponseEntity.ok(new Response("success", "Update booking successfully.", null));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/status/{bookingId}")
    public ResponseEntity<?> updateBookingStatusAdmin(@PathVariable Long bookingId,
                                                     @RequestParam(defaultValue = "")  String status) throws DataNotFoundException {
        Booking booking = bookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok(new Response("success", "Update booking status by admin successfully.", null));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) throws DataNotFoundException {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(new Response("success", "Delete booking successfully.", null));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DOCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(new Response("success", "Get booking successfully.", BookingResponse.fromBookingResponse(booking)));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(defaultValue = "") LocalDate dateBooking,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "999999999") int limit,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by(sortDirection,"createdAt")
        );
        BookingListResponse bookings = bookingService.getAllBookings(dateBooking, keyword, status, pageRequest);
        return ResponseEntity.ok(new Response("success", "Get booking all successfully.", bookings));
    }


    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_USER') and #userId == principal.id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId, Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId).stream()
                .map(BookingResponse::fromBookingResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response("success", "Get bookings by user successfully.", bookings));
    }

    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<?> getBookingsByUserIdAndStatus(@PathVariable Long userId,
                                                          @RequestParam(defaultValue = "paid") String status
    ) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserIdAndStatus(userId, status).stream()
                .map(BookingResponse::fromBookingResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response("success", "Get bookings by user successfully.", bookings));
    }


    @PreAuthorize("hasRole('ROLE_USER') and #userId == principal.id")
    @GetMapping("/user/{userId}/detail")
    public ResponseEntity<?> getBookingUserById(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "") Long bookingId,
                                                Authentication authentication
                                                ) {

        Booking booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(new Response("success", "Get booking successfully.", BookingResponse.fromBookingResponse(booking)));    }


    @PreAuthorize("hasRole('ROLE_USER') and #userId == principal.id")
    @PutMapping("/user/{userId}/detail")
    public ResponseEntity<?> updateBookingStatusUser(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "") Long bookingId,
                                                     @RequestBody RefundInvoiceDTO refundInvoiceDTO,
                                                     Authentication authentication
    ) throws DataNotFoundException {
        String status = Booking.WAITREFUND;
        Booking booking = bookingService.updateBookingStatusUser(bookingId, status, refundInvoiceDTO);
        return ResponseEntity.ok(new Response("success", "Request refund successfully.", null));
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/payBooking/{id}")
    public ResponseEntity<?> updateInfoPaymentOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String vnp_TransactionNo,
            @RequestParam(defaultValue = "") String vnp_ResponseCode
    ) throws DataNotFoundException {
        BookingResponse bookingResponse = bookingService.updateInfoPaymentBooking(id, vnp_TransactionNo, vnp_ResponseCode);
        return ResponseEntity.ok(new Response("success", "Booking payment successfully.", null));
    }



    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    @GetMapping("/doctor")
    public ResponseEntity<?> getAllBookingsDoctor(
            @RequestParam(defaultValue = "") LocalDate dateBooking,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "999999999") int limit,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") Long scheduleId

    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by(sortDirection,"id")
        );
        BookingListResponse bookings = bookingService.getAllBookingsDoctor(dateBooking, keyword, status,scheduleId, pageRequest);
        return ResponseEntity.ok(new Response("success", "Get booking all successfully.", bookings));
    }

    @PreAuthorize("hasRole('ROLE_USER') and #userId == principal.id")
    @PutMapping("/user/{userId}/change")
    public ResponseEntity<?> updateBookingStatusUser(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "") Long bookingId,
                                                     @RequestParam(defaultValue = "") Long scheduleId,

                                                     Authentication authentication
    ) throws DataNotFoundException {
        Booking booking = bookingService.changeBookingByUser(bookingId, scheduleId);
        return ResponseEntity.ok(new Response("success", "Request refund successfully.", null));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/last7days")
    public  ResponseEntity<?> getBookingsInLast7Days() {
        List<BookingResponse> bookingResponseList = bookingService.getBookingsInLast7Days();
        return ResponseEntity.ok(new Response("success", "Request refund successfully.", bookingResponseList));

    }

}
