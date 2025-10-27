package com.example.healthcaresystem.controller;

import com.example.healthcaresystem.model.Appointment;
import com.example.healthcaresystem.model.Patient;
import com.example.healthcaresystem.repo.AppointmentRepository;
import com.example.healthcaresystem.repo.PatientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientMedicalRecords(@PathVariable Long patientId) {
        try {
            Optional<Patient> patient = patientRepository.findById(patientId);
            if (patient.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
            }

            List<Appointment> appointments = appointmentRepository.findByPatientOrderByAppointmentTimeDesc(patient.get());
            
            // Filter appointments that have notes/prescriptions or are completed
            List<Appointment> medicalRecords = appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.Status.COMPLETED && 
                              apt.getNotes() != null && !apt.getNotes().trim().isEmpty())
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("patientId", patientId);
            response.put("patientName", patient.get().getName());
            response.put("patientEmail", patient.get().getEmail());
            response.put("patientPhone", patient.get().getPhone());
            response.put("patientDob", patient.get().getDob());
            response.put("medicalHistory", patient.get().getMedicalHistory());
            response.put("medicalRecords", medicalRecords);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get medical records: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}/appointments")
    public ResponseEntity<?> getAllPatientAppointments(@PathVariable Long patientId) {
        try {
            Optional<Patient> patient = patientRepository.findById(patientId);
            if (patient.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
            }

            List<Appointment> appointments = appointmentRepository.findByPatientOrderByAppointmentTimeDesc(patient.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("patientId", patientId);
            response.put("patientName", patient.get().getName());
            response.put("appointments", appointments);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get appointments: " + e.getMessage()));
        }
    }

    @PutMapping("/patient/{patientId}/medical-history")
    public ResponseEntity<?> updateMedicalHistory(
            @PathVariable Long patientId,
            @RequestBody Map<String, String> request) {
        try {
            Optional<Patient> patient = patientRepository.findById(patientId);
            if (patient.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
            }

            String medicalHistory = request.get("medicalHistory");
            if (medicalHistory == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "medicalHistory is required"));
            }

            Patient pat = patient.get();
            pat.setMedicalHistory(medicalHistory);
            patientRepository.save(pat);

            return ResponseEntity.ok(Map.of("message", "Medical history updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update medical history: " + e.getMessage()));
        }
    }
}
