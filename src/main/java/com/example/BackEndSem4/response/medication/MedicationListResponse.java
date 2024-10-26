package com.example.BackEndSem4.response.medication;

import com.example.BackEndSem4.models.Medication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MedicationListResponse {
    private List<Medication> medications;
    private int totalPages;
}
