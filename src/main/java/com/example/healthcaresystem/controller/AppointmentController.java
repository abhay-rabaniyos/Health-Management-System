package com.example.healthcaresystem.controller;

import com.example.healthcaresystem.model.Appointment;
import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.Patient;
import com.example.healthcaresystem.repo.AppointmentRepository;
import com.example.healthcaresystem.repo.DoctorRepository;
import com.example.healthcaresystem.repo.PatientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long patientId = Long.valueOf(request.get("patientId").toString());
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            String appointmentTimeStr = request.get("appointmentTime").toString();

            // Validate required fields
            if (patientId == null || doctorId == null || appointmentTimeStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "patientId, doctorId, and appointmentTime are required"));
            }

            // Parse appointment time
            LocalDateTime appointmentTime = LocalDateTime.parse(appointmentTimeStr);

            // Validate appointment time is in the future
            if (appointmentTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Appointment time must be in the future"));
            }

            // Check if doctor and patient exist
            Optional<Doctor> doctor = doctorRepository.findById(doctorId);
            Optional<Patient> patient = patientRepository.findById(patientId);

            if (doctor.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
            }
            if (patient.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
            }

            // Check for double booking
            boolean isDoubleBooked = appointmentRepository.existsByDoctorAndAppointmentTimeAndStatus(
                doctor.get(), appointmentTime, Appointment.Status.SCHEDULED);
            
            if (isDoubleBooked) {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctor is already booked at this time"));
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor.get());
            appointment.setPatient(patient.get());
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus(Appointment.Status.SCHEDULED);

            appointmentRepository.save(appointment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment booked successfully");
            response.put("appointmentId", appointment.getId());
            response.put("doctorName", doctor.get().getName());
            response.put("patientName", patient.get().getName());
            response.put("appointmentTime", appointmentTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to book appointment: " + e.getMessage()));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long appointmentId = Long.valueOf(request.get("appointmentId").toString());

            Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
            if (appointment.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Appointment not found"));
            }

            Appointment apt = appointment.get();
            
            // Check if appointment is already cancelled or completed
            if (apt.getStatus() != Appointment.Status.SCHEDULED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Appointment cannot be cancelled"));
            }

            // Cancel the appointment
            apt.setStatus(Appointment.Status.CANCELLED);
            appointmentRepository.save(apt);

            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to cancel appointment: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long patientId) {
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
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get patient appointments: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorSchedule(@PathVariable Long doctorId) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(doctorId);
            if (doctor.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
            }

            List<Appointment> appointments = appointmentRepository.findByDoctorOrderByAppointmentTimeDesc(doctor.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("doctorId", doctorId);
            response.put("doctorName", doctor.get().getName());
            response.put("specialization", doctor.get().getSpecialization());
            response.put("appointments", appointments);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get doctor schedule: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/prescription")
    public ResponseEntity<?> addPrescription(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Optional<Appointment> appointment = appointmentRepository.findById(id);
            if (appointment.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Appointment not found"));
            }

            Appointment apt = appointment.get();
            
            // Check if appointment is completed
            if (apt.getStatus() != Appointment.Status.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Can only add prescription to completed appointments"));
            }

            String prescription = request.get("prescription");
            if (prescription == null || prescription.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Prescription is required"));
            }

            apt.setNotes(prescription);
            appointmentRepository.save(apt);

            return ResponseEntity.ok(Map.of("message", "Prescription added successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to add prescription: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) {
        try {
            Optional<Appointment> appointment = appointmentRepository.findById(id);
            if (appointment.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Appointment not found"));
            }

            Appointment apt = appointment.get();
            
            // Check if appointment is scheduled
            if (apt.getStatus() != Appointment.Status.SCHEDULED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only scheduled appointments can be completed"));
            }

            apt.setStatus(Appointment.Status.COMPLETED);
            appointmentRepository.save(apt);

            return ResponseEntity.ok(Map.of("message", "Appointment completed successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to complete appointment: " + e.getMessage()));
        }
    }
}
