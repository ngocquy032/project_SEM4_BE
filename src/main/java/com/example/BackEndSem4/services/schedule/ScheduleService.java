package com.example.BackEndSem4.services.schedule;

import com.example.BackEndSem4.dtos.ScheduleDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Booking;
import com.example.BackEndSem4.models.Clinic;
import com.example.BackEndSem4.models.Doctor;
import com.example.BackEndSem4.models.Schedule;
import com.example.BackEndSem4.repositories.BookingRepository;
import com.example.BackEndSem4.repositories.ClinicRepository;
import com.example.BackEndSem4.repositories.DoctorRepository;
import com.example.BackEndSem4.repositories.ScheduleReponsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ScheduleService implements IScheduleService{

    private final ScheduleReponsitory scheduleRepository;

    private final BookingRepository bookingRepository;

    private final DoctorRepository doctorRepository;

    private final ClinicRepository clinicRepository;


    @Override
    public Page<Schedule> getAllSchedules(Long specialtyId, Long doctorId, LocalDate dateSchedule,
                                          String keyword, Pageable pageable) {


        return scheduleRepository.getAllSchedules(specialtyId, doctorId, dateSchedule, keyword, pageable);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }


    @Override
    @Transactional
    public Schedule createSchedule(ScheduleDTO scheduleDTO) throws DataNotFoundException {

        Doctor doctor = doctorRepository.findById(scheduleDTO.getDoctorId())
                .orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        Clinic clinic = clinicRepository.findById(scheduleDTO.getClinicId())
                .orElseThrow(() -> new DataNotFoundException("Clinic not found"));

        // Kiểm tra xem bác sĩ đó có lịch vào khung giờ đó chưa
        Schedule scheduleExisting = scheduleRepository.findScheduleCheck(
                scheduleDTO.getDoctorId(),scheduleDTO.getStartTime(), scheduleDTO.getDateSchedule());

        if(scheduleExisting != null) {
            throw new DataNotFoundException("The schedule already exists, please check again.");
        }

        Schedule schedule = Schedule.builder()
                .doctor(doctor)
                .clinic(clinic)
                .startTime(scheduleDTO.getStartTime())
                .endTime(scheduleDTO.getEndTime())
                .dateSchedule(scheduleDTO.getDateSchedule())
                .price(scheduleDTO.getPrice())
                .bookingLimit(scheduleDTO.getBookingLimit())
                .numberBooked(0)
                .active(true)
                .build();

        return scheduleRepository.save(schedule);
    }



    @Override
    @Transactional
    public Schedule updateSchedule(Long id, ScheduleDTO scheduleDTO) throws DataNotFoundException {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Doctor doctor = doctorRepository.findById(scheduleDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Clinic clinic = clinicRepository.findById(scheduleDTO.getClinicId())
                .orElseThrow(() -> new RuntimeException("Clinic not found"));

        // Kiểm tra xem bác sĩ đó có lịch vào khung giờ đó chưa
        Schedule scheduleExisting = scheduleRepository.findScheduleCheck(
                scheduleDTO.getDoctorId(),scheduleDTO.getStartTime(), scheduleDTO.getDateSchedule());

        if(scheduleExisting != null && !scheduleExisting.getId().equals(id)) {
            throw new DataNotFoundException("Repeat data schedule with other schedules, please check again.");
        }

        schedule.setDoctor(doctor);
        schedule.setClinic(clinic);
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setDateSchedule(scheduleDTO.getDateSchedule());
        schedule.setPrice(scheduleDTO.getPrice());
        schedule.setBookingLimit(scheduleDTO.getBookingLimit());
        schedule.setActive(scheduleDTO.isActive());

        schedule.setUpdatedAt(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }



    @Override
    @Transactional
    public void deleteSchedule(Long id) throws DataNotFoundException {
        // Check kiểm tra để xóa mềm hay xóa cứng
        List<Booking> bookingList = bookingRepository.findAllByScheduleId(id);
        if(!bookingList.isEmpty()) {
            throw new DataNotFoundException("A booking has been made and cannot be deleted.");
        }

        scheduleRepository.deleteById(id);
    }



    @Override
    public List<Schedule> getSchedulesByDoctorId(Long doctorId, LocalDate dateSchedule) {
        return scheduleRepository.findByDoctorId(doctorId, dateSchedule);
    }


    @Override
    @Transactional
    public void extendSchedulesForNextWeek() throws DataNotFoundException{
        // Giả sử hiện tại là ngày 02/10/2024
        LocalDate currentDate = LocalDate.now(); // Hoặc gán ngày hiện tại là 02/10/2024
        LocalDate startDate = currentDate.minusDays(7); // Ngày bắt đầu: 25/09/2024
        LocalDate endDate = currentDate.minusDays(1); // Lấy đến ngày trước đó

        List<Schedule> existingSchedules = scheduleRepository.findSchedulesInRange(startDate, endDate);

        if (existingSchedules.isEmpty()) {
            // Không có lịch nào cần xử lý
            throw  new DataNotFoundException("There are no schedules in the last 7 days for renewal.");
        }

        Schedule farthestSchedule = existingSchedules.get(0);
        for (Schedule schedule : existingSchedules) {
            if (schedule.getDateSchedule().isBefore(farthestSchedule.getDateSchedule())) {
                farthestSchedule = schedule;
            }
        }

        for (Schedule schedule : existingSchedules) {
            LocalDate newDateSchedule;

            if (schedule.equals(farthestSchedule)) {
                // Lịch có ngày xa nhất, cộng 14 ngày
                newDateSchedule = schedule.getDateSchedule().plusDays(14);
            } else {
                // Các lịch còn lại, cộng 7 ngày
                newDateSchedule = schedule.getDateSchedule().plusDays(7);
            }

            // Kiểm tra xem lịch này đã tồn tại hay chưa (tránh trùng lặp)
            Schedule existingSchedule = scheduleRepository.findScheduleCheck(
                    schedule.getDoctor().getId(),
                    schedule.getStartTime(),
                    newDateSchedule
            );

            // Nếu không có lịch trùng lặp, thì lưu lại lịch mới
            if (existingSchedule == null) {
                Schedule newSchedule = Schedule.builder()
                        .doctor(schedule.getDoctor())
                        .clinic(schedule.getClinic())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .dateSchedule(newDateSchedule)
                        .price(schedule.getPrice())
                        .bookingLimit(schedule.getBookingLimit())
                        .numberBooked(0)  // Reset số lượng đã đặt về 0
                        .active(true)     // Set trạng thái active
                        .build();

                // Lưu lịch mới
                scheduleRepository.save(newSchedule);

            }
        }
    }

}
