package com.example.BackEndSem4.services.booking;

import com.example.BackEndSem4.dtos.BookingDTO;
import com.example.BackEndSem4.dtos.RefundInvoiceDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.*;
import com.example.BackEndSem4.repositories.*;
import com.example.BackEndSem4.response.booking.BookingListResponse;
import com.example.BackEndSem4.response.booking.BookingResponse;
import com.example.BackEndSem4.services.refundInvoice.RefundInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService{

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final ScheduleReponsitory scheduleRepository;

    private final HistoryRepository historyRepository;

    private final RefundInvoiceService refundInvoiceService;
    private  final RefundInvoiceRepository refundInvoiceRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) throws DataNotFoundException {
        User user = userRepository.findById(bookingDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
                .orElseThrow(() -> new DataNotFoundException("Schedule not found"));

        if (schedule.getNumberBooked() >= schedule.getBookingLimit()) {
            throw new DataNotFoundException("This schedule has reached its set limit.Please try again.");
        }
        // Kết hợp tất cả các chuỗi lại
        Long idNumber = generateUniqueScheduleId(schedule, bookingDTO);

        List<Booking> bookings = bookingRepository.findAllByUserId(bookingDTO.getUserId());
        if(bookings.size() >0) {
            if(bookings.get(0).getStatus().equals(Booking.PAID) && bookings.get(0).getSchedule().getId().equals(bookingDTO.getScheduleId())) {
                throw new DataNotFoundException("Appointment exists, cannot be rescheduled.");

            }
        }
        Booking booking = Booking.builder()
                .id(idNumber)
                .user(user)
                .schedule(schedule)
                .amount(bookingDTO.getAmount())
                .paymentMethod(bookingDTO.getPaymentMethod())
                .reason(bookingDTO.getReason())
                .changeCount(0)
                .status(Booking.PENDING)
                .build();

        Booking booking1 = bookingRepository.save(booking);

        History history = History.builder()
                .booking(booking1)
                .build();

        History history1 = historyRepository.save(history);
        schedule.setNumberBooked(schedule.getNumberBooked() + 1);
        scheduleRepository.save(schedule);
        return booking1;
    }


    // GenerateId
    public Long generateUniqueScheduleId(Schedule schedule, BookingDTO bookingDTO) {
        String doctorId = schedule.getDoctor().getId().toString();
        String userId = bookingDTO.getUserId().toString();
        String startTime = schedule.getStartTime().format(DateTimeFormatter.ofPattern("HHmm"));
        String dateSchedule = schedule.getDateSchedule().format(DateTimeFormatter.ofPattern("ddMMyy"));

        Random random = new Random();
        Long idNumber;
        boolean idExists;

        // Vòng lặp để tìm một idNumber không trùng với bất kỳ schedule nào
        do {
            // Tạo số ngẫu nhiên 2 chữ số
            int randomTwoDigits = 10 + random.nextInt(90);

            // Kết hợp tất cả các chuỗi lại
            idNumber = Long.valueOf(doctorId + startTime + dateSchedule + userId + randomTwoDigits);

            // Kiểm tra xem idNumber có tồn tại trong cơ sở dữ liệu hay không
            Optional<Schedule> existingSchedule = scheduleRepository.findById(idNumber);
            idExists = existingSchedule.isPresent(); // true nếu id đã tồn tại

        } while (idExists); // Lặp lại nếu id đã tồn tại

        return idNumber;
    }

    @Override
    @Transactional
    public Booking updateBooking(Long id, BookingDTO bookingDTO) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));

        booking.setPaymentMethod(bookingDTO.getPaymentMethod());
        booking.setPaymentCode(bookingDTO.getPaymentCode());
        booking.setStatus(bookingDTO.getStatus());
        booking.setUpdatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBookingStatus(Long id, String status) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        booking.setStatus(status);
        booking.setUpdatedAt(LocalDateTime.now());

        if(status.equals(Booking.CANCELLED)) {
            Schedule schedule = booking.getSchedule();
            schedule.setNumberBooked(schedule.getNumberBooked() -1);
            scheduleRepository.save(schedule);
        }

        if(status.equals(Booking.REFUNDED)) {
            RefundInvoice refundInvoice = refundInvoiceRepository.findByBooking_Id(id);
            refundInvoice.setStatus(RefundInvoice.REFUNDED);
            refundInvoiceRepository.save(refundInvoice);
        }

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBookingStatusUser(Long id, String status, RefundInvoiceDTO refundInvoiceDTO) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));

        if (!canRejectedBooking(booking)) {
            throw new DataNotFoundException("Booking cannot be refund within 2 hours of the scheduled time.");
        }

        booking.setStatus(status);
        booking.setUpdatedAt(LocalDateTime.now());
        Schedule schedule = booking.getSchedule();
        schedule.setNumberBooked(schedule.getNumberBooked() -1);
        scheduleRepository.save(schedule);

        refundInvoiceService.createRefundInvoice(refundInvoiceDTO);
        return bookingRepository.save(booking);
    }


    public boolean canRejectedBooking(Booking booking) {
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        // Kết hợp dateSchedule và startTime để có thời gian lịch khám đầy đủ
        LocalDateTime scheduleDateTime = LocalDateTime.of(booking.getSchedule().getDateSchedule(), booking.getSchedule().getStartTime());
        // Kiểm tra nếu thời gian hiện tại nằm trong khoảng 1 ngày trước khi lịch khám bắt đầu
        LocalDateTime oneDayBeforeSchedule = scheduleDateTime.minusHours(2);

        return now.isBefore(oneDayBeforeSchedule);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) throws DataNotFoundException {
        if (!bookingRepository.existsById(id)) {
            throw new DataNotFoundException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    @Override
    public BookingListResponse getAllBookings(LocalDate dateBooking, String keyword,String status, Pageable pageable) {
        Page<BookingResponse> bookings = bookingRepository.findAllBookings(dateBooking, keyword, status, pageable)
                .map(BookingResponse::fromBookingResponse);

        int totalPages = bookings.getTotalPages();
        List<BookingResponse> bookingListResponses = bookings.getContent();

        return BookingListResponse.builder()
                .bookingList(bookingListResponses)
                .totalPages(totalPages)
                .build();
        }

    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findAllByUserId(userId);
    }

    @Override
    public List<Booking> getBookingsByUserIdAndStatus(Long userId, String status) {
        return bookingRepository.findAllByUserIdAndStatus(userId, status);
    }
    @Override
    public BookingResponse updateInfoPaymentBooking(Long id, String vnp_TransactionNo, String vnp_ResponseCode) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find booking with id: " + id));

        if (vnp_TransactionNo != null && !vnp_TransactionNo.isEmpty() && "00".equals(vnp_ResponseCode)) {
            booking.setPaymentCode(vnp_TransactionNo);
            booking.setStatus(Booking.PAID);
        }


        Booking bookingResponse = bookingRepository.save(booking);
        return BookingResponse.fromBookingResponse(bookingResponse);
    }


    @Override
    public BookingListResponse getAllBookingsDoctor(LocalDate dateBooking, String keyword,String status, Long scheduleId, Pageable pageable) {
        Page<BookingResponse> bookings = bookingRepository.findAllBookingsDoctor(dateBooking, keyword, status, scheduleId, pageable)
                .map(BookingResponse::fromBookingResponse);

        int totalPages = bookings.getTotalPages();
        List<BookingResponse> bookingListResponses = bookings.getContent();

        return BookingListResponse.builder()
                .bookingList(bookingListResponses)
                .totalPages(totalPages)
                .build();
    }



    @Override
    @Transactional
    public Booking changeBookingByUser(Long id, Long scheduleId) throws DataNotFoundException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException("Schedule not found"));

        if (schedule.getNumberBooked() >= schedule.getBookingLimit()) {
            throw new DataNotFoundException("This schedule has reached its set limit.Please try again.");
        }

        if (booking.getChangeCount() >=1) {
            throw new DataNotFoundException("You can only change your appointment once.");
        }

        booking.setSchedule(schedule);
        booking.setChangeCount(1);
        booking.setUpdatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public List<BookingResponse> getBookingsInLast7Days() {
        // Tính ngày hiện tại và 7 ngày trước
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // Gọi phương thức trong repository để lấy dữ liệu
        List<Booking> bookings = bookingRepository.findBookingsInLast7Days(sevenDaysAgo, now);


        return bookings.stream()
                .map(BookingResponse::fromBookingResponse) // Sử dụng phương thức tham chiếu
                .collect(Collectors.toList());
    }

}
