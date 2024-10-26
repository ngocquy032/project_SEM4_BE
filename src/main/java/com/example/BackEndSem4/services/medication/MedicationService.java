package com.example.BackEndSem4.services.medication;


import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Medication;
import com.example.BackEndSem4.repositories.MedicationRepository;
import com.example.BackEndSem4.response.medication.MedicationListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    // Get all medications
    public MedicationListResponse getAllMedications(String keyword, Pageable pageable) {
        Page<Medication> medications = medicationRepository.findAllMedications(keyword, pageable);
        int totalPages = medications.getTotalPages();
        List<Medication> medicationList = medications.getContent();

        return MedicationListResponse.builder()
                .medications(medicationList)
                .totalPages(totalPages)
                .build();
    }

    // Get medication by ID
    public Medication getMedicationById(Long id) throws DataNotFoundException {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Medication not found by id: " + id));
    }

    // Create new medication
    @Transactional
    public Medication createMedication(Medication medication) throws DataNotFoundException {
        // Check if medication name already exists
        Optional<Medication> existingMedication = medicationRepository.findByMedicationName(medication.getMedicationName());
        if (existingMedication.isPresent()) {
            throw new DataNotFoundException("Medication name already exists.");
        }
        return medicationRepository.save(medication);
    }

    // Update existing medication
    @Transactional
    public Medication updateMedication(Long id, Medication medicationDetails) throws DataNotFoundException {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Medication not found."));

        // Check if the new medication name already exists (excluding current one)
        Optional<Medication> existingMedication = medicationRepository.findByMedicationName(medicationDetails.getMedicationName());
        if (existingMedication.isPresent() && !existingMedication.get().getId().equals(id)) {
            throw new DataNotFoundException("Medication name already exists.");
        }

        medication.setMedicationName(medicationDetails.getMedicationName());
        return medicationRepository.save(medication);
    }

    // Delete medication
    @Transactional
    public void deleteMedication(Long id) throws Exception {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new Exception("Medication not found."));
        medicationRepository.delete(medication);
    }
}