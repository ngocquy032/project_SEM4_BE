package com.example.BackEndSem4.services.specialty;

import com.example.BackEndSem4.dtos.SpecialtyDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Doctor;
import com.example.BackEndSem4.models.Specialty;
import com.example.BackEndSem4.repositories.DoctorRepository;
import com.example.BackEndSem4.repositories.SpecialtyRepository;
import com.example.BackEndSem4.services.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SpecialtyService implements ISpecialtyService{
    private final SpecialtyRepository specialtyRepository;
    private final ImageService imageService;
    private final DoctorRepository doctorRepository;

    @Override
    public Page<Specialty> getSpecialtyAll(String keyword, Pageable pageable) {
        return specialtyRepository.getSpecialtyAll(keyword, pageable);
    }

    @Override
    public Specialty getSpecialtyById(Long id) throws DataNotFoundException {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Specialty not found with id " + id));

    }

    @Override
    @Transactional
    public Specialty createSpecialty(SpecialtyDTO specialtyDTO) throws DataNotFoundException {
        Optional<Specialty> existingSpecialty =
                specialtyRepository.findBySpecialtyName(specialtyDTO.getSpecialtyName());
        if (existingSpecialty.isPresent()) {
            throw new DataNotFoundException("Specialty already exists with name " + specialtyDTO.getSpecialtyName());
        }
        Specialty specialtynew = Specialty.builder()
                .specialtyName(specialtyDTO.getSpecialtyName())
                .specialtyImage(specialtyDTO.getSpecialtyImage())
                .price(specialtyDTO.getPrice())
                .description(specialtyDTO.getDescription())
                .build();
        specialtynew.setCreatedAt(LocalDateTime.now());
        specialtynew.setUpdatedAt(LocalDateTime.now());
        return specialtyRepository.save(specialtynew);
    }

    @Override
    @Transactional
    public Specialty updateSpecialty(Long id, SpecialtyDTO specialtyDTO) throws Exception {
        Specialty existingSpecialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Specialty not found with id " + id));

        Optional<Specialty> specialtyWithSameName = specialtyRepository.findBySpecialtyName(specialtyDTO.getSpecialtyName());

        if (specialtyWithSameName.isPresent() && !specialtyWithSameName.get().getId().equals(id)) {
            throw new DataNotFoundException("Specialty already exists with name " + specialtyDTO.getSpecialtyName());
        }
        String urlImageOld = existingSpecialty.getSpecialtyImage();

        existingSpecialty.setSpecialtyName(specialtyDTO.getSpecialtyName());
        existingSpecialty.setDescription(specialtyDTO.getDescription());
        existingSpecialty.setPrice(specialtyDTO.getPrice());
        existingSpecialty.setUpdatedAt(LocalDateTime.now());
        if(specialtyDTO.getSpecialtyImage() != null) {
            existingSpecialty.setSpecialtyImage(specialtyDTO.getSpecialtyImage());
            imageService.deleteFile(urlImageOld);
        }

        return specialtyRepository.save(existingSpecialty);
    }



    @Override
    @Transactional
    public void deleteSpecialtyById(Long id) throws Exception {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Specialty not found with id " + id));

        List<Doctor> doctors = doctorRepository.findAllBySpecialtyId(id);

        if (!doctors.isEmpty()) {
            throw new DataNotFoundException("Doctor already exists and cannot be delete.");
        }

        imageService.deleteFile(specialty.getSpecialtyImage());
        specialtyRepository.delete(specialty);
    }

    // Delete file image with file uploads

}
