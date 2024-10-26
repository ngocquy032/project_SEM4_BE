package com.example.BackEndSem4.controllers;


import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Medication;
import com.example.BackEndSem4.response.Response;
import com.example.BackEndSem4.response.medication.MedicationListResponse;
import com.example.BackEndSem4.services.medication.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    // Get all medications
    @GetMapping("")
    public ResponseEntity<?> getAllMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "999999999") int limit,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestParam(defaultValue = "") String keyword
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by(sortDirection,"id")
        );

        MedicationListResponse medications = medicationService.getAllMedications(keyword,pageRequest);
        return ResponseEntity.ok(new Response("success", "Get Medications successfully.",medications));
    }

    // Get medication by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicationById(@PathVariable Long id) throws DataNotFoundException {
        Medication medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(new Response("success", "Get Medication successfully.",medication));

    }

    // Create a new medication
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<?> createMedication(@RequestBody Medication medication) throws Exception {
            Medication newMedication = medicationService.createMedication(medication);
        return ResponseEntity.ok(new Response("success", "Create Medication successfully.",newMedication));

    }

    // Update an existing medication
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedication(@PathVariable Long id, @RequestBody Medication medicationDetails) throws Exception {
        Medication updatedMedication = medicationService.updateMedication(id, medicationDetails);
        return ResponseEntity.ok(new Response("success", "Update Medication successfully.",null));

    }

    // Delete a medication
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedication(@PathVariable Long id) throws Exception {

            medicationService.deleteMedication(id);
        return ResponseEntity.ok(new Response("success", "Delete Medication successfully.",null));

    }
}