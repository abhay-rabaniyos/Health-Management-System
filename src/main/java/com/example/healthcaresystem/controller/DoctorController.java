package com.example.healthcaresystem.controller;

import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.DoctorAvailableSlot;
import com.example.healthcaresystem.repo.DoctorRepository;
import com.example.healthcaresystem.repo.DoctorAvailableSlotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailableSlotRepository slotRepository;

    @GetMapping
    public ResponseEntity<?> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Doctor> doctors;
            
            if (specialization != null && !specialization.trim().isEmpty()) {
                doctors = doctorRepository.findBySpecializationContainingIgnoreCase(specialization, pageable);
            } else {
                doctors = doctorRepository.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctors.getContent());
            response.put("currentPage", doctors.getNumber());
            response.put("totalItems", doctors.getTotalElements());
            response.put("totalPages", doctors.getTotalPages());
            response.put("hasNext", doctors.hasNext());
            response.put("hasPrevious", doctors.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to search doctors: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isPresent()) {
                return ResponseEntity.ok(doctor.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get doctor: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable Long id) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<DoctorAvailableSlot> availableSlots = slotRepository
                .findByDoctorAndAvailableTimeAfter(doctor.get(), LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("doctorId", id);
            response.put("doctorName", doctor.get().getName());
            response.put("specialization", doctor.get().getSpecialization());
            response.put("availableSlots", availableSlots);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get availability: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/slots")
    public ResponseEntity<?> addAvailableSlot(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String timeStr = request.get("availableTime");
            if (timeStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "availableTime is required"));
            }

            LocalDateTime availableTime = LocalDateTime.parse(timeStr);
            
            // Check if slot already exists
            if (slotRepository.existsByDoctorAndAvailableTime(doctor.get(), availableTime)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Slot already exists for this time"));
            }

            DoctorAvailableSlot slot = new DoctorAvailableSlot();
            slot.setDoctor(doctor.get());
            slot.setAvailableTime(availableTime);
            
            slotRepository.save(slot);
            
            return ResponseEntity.ok(Map.of("message", "Available slot added successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to add slot: " + e.getMessage()));
        }
    }
}
