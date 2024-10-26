package com.example.BackEndSem4.services.clinic;

import com.example.BackEndSem4.dtos.ClinicDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Clinic;
import com.example.BackEndSem4.models.Schedule;
import com.example.BackEndSem4.repositories.ClinicRepository;
import com.example.BackEndSem4.repositories.ScheduleReponsitory;
import com.example.BackEndSem4.services.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ClinicService implements IClinicService{

    private final ClinicRepository clinicRepository;
    private final ImageService imageService;
    private final ScheduleReponsitory scheduleReponsitory;


    @Override
    public Page<Clinic> getClinicAll(String keyword, Pageable pageable) {
        return clinicRepository.getClinicAll(keyword, pageable);
    }

    @Override
    public Clinic getClinicById(Long id) throws DataNotFoundException {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Clinic not found with id " + id));
    }

    @Override
    @Transactional
    public Clinic createClinic(ClinicDTO clinicDTO) throws DataNotFoundException {
        Optional<Clinic> existingClinic =
                clinicRepository.findByClinicNameAndAddress(clinicDTO.getClinicName(), clinicDTO.getAddress());
        if (existingClinic.isPresent()) {
            throw new DataNotFoundException("Clinic already exists with name " + clinicDTO.getClinicName() + " and address" + clinicDTO.getAddress());
        }
        Clinic clinicNew = Clinic.builder()
                .clinicName(clinicDTO.getClinicName())
                .clinicImage(clinicDTO.getClinicImage())
                .address(clinicDTO.getAddress())
                .phone(clinicDTO.getPhone())
                .email(clinicDTO.getEmail())
                .active(true)
                .build();
        clinicNew.setCreatedAt(LocalDateTime.now());
        clinicNew.setUpdatedAt(LocalDateTime.now());
        return clinicRepository.save(clinicNew);
    }

    @Override
    @Transactional
    public Clinic updateClinic(Long id, ClinicDTO clinicDTO) throws Exception {
        Clinic existingClinic = clinicRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Clinic not found with id " + id));

        Optional<Clinic> clinicWithSameNameAndAddress = clinicRepository.findByClinicNameAndAddress(clinicDTO.getClinicName(),clinicDTO.getAddress());

        if (clinicWithSameNameAndAddress.isPresent() && !clinicWithSameNameAndAddress.get().getId().equals(id)) {
            throw new DataNotFoundException("Clinic already exists with name " + clinicDTO.getClinicName() + " and address" + clinicDTO.getAddress());
        }
        String urlImageOld = existingClinic.getClinicImage();

        existingClinic.setClinicName(clinicDTO.getClinicName());
        if(clinicDTO.getClinicImage() != null) {
            existingClinic.setClinicImage(clinicDTO.getClinicImage());
            imageService.deleteFile(urlImageOld);

        }
        existingClinic.setPhone(clinicDTO.getPhone());
        existingClinic.setEmail(clinicDTO.getEmail());
        existingClinic.setAddress(clinicDTO.getAddress());
        existingClinic.setActive(clinicDTO.isActive());
        existingClinic.setUpdatedAt(LocalDateTime.now());

        return clinicRepository.save(existingClinic);
    }

    @Override
    @Transactional
    public void deleteClinicById(Long id) throws Exception {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Clinic not found with id " + id));

        List<Schedule> scheduleList = scheduleReponsitory.findAllByClinicId(id);

        if(!scheduleList.isEmpty()) {
            throw new DataNotFoundException("There is a schedule with the clinic cannot delete.");
        }

        imageService.deleteFile(clinic.getClinicImage());
        clinicRepository.delete(clinic);
    }
}
