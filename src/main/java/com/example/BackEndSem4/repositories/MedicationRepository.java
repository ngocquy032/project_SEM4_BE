package com.example.BackEndSem4.repositories;

import com.example.BackEndSem4.models.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Optional<Medication> findByMedicationName(String medicationName);

    @Query("SELECT m FROM Medication m WHERE LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Medication> findAllMedications(@Param("keyword") String keyword, Pageable pageable);}