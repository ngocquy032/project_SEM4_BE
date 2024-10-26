package com.example.BackEndSem4.services.booking;

import com.example.BackEndSem4.dtos.BookingDTO;
import com.example.BackEndSem4.dtos.RefundInvoiceDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Booking;
import com.example.BackEndSem4.models.RefundInvoice;
import com.example.BackEndSem4.response.booking.BookingListResponse;
import com.example.BackEndSem4.response.booking.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IBookingService {
    Booking createBooking(BookingDTO bookingDTO) throws DataNotFoundException;
    Booking updateBooking(Long id, BookingDTO bookingDTO) throws DataNotFoundException;
    Booking updateBookingStatus(Long id, String status) throws DataNotFoundException;
    Booking updateBookingStatusUser(Long id, String status, RefundInvoiceDTO refundInvoiceDTO) throws DataNotFoundException;

    void deleteBooking(Long id) throws DataNotFoundException;
    Booking getBookingById(Long id);
    BookingListResponse getAllBookings(LocalDate dateBooking, String keyword,String status, Pageable pageable);
    List<Booking> getBookingsByUserId(Long userId);
     List<Booking> getBookingsByUserIdAndStatus(Long userId, String status);

     BookingResponse updateInfoPaymentBooking(Long id, String vnp_TransactionNo, String vnp_ResponseCode) throws DataNotFoundException;


    BookingListResponse getAllBookingsDoctor(LocalDate dateBooking, String keyword,String status,Long scheduleId,  Pageable pageable);

    Booking changeBookingByUser(Long id, Long scheduleId) throws DataNotFoundException;


}
