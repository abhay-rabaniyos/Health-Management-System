package com.example.healthcaresystem.controller;

import com.example.healthcaresystem.model.AuthRequest;
import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.Patient;
import com.example.healthcaresystem.repo.DoctorRepository;
import com.example.healthcaresystem.repo.PatientRepository;
import com.example.healthcaresystem.service.JwtService;
import com.example.healthcaresystem.service.UnifiedUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UnifiedUserDetailsService unifiedUserDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            String phone = request.get("phone");
            String dobStr = request.get("dob");

            // Validate required fields
            if (email == null || password == null || name == null || phone == null || dobStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }

            // Check if email already exists
            if (unifiedUserDetailsService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            // Parse date of birth
            LocalDate dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);

            // Create Patient entity
            Patient patient = new Patient();
            patient.setName(name);
            patient.setEmail(email);
            patient.setPhone(phone);
            patient.setDob(dob);
            patient.setPassword(password); // Will be encrypted by service

            // Save patient using unified service
            unifiedUserDetailsService.addPatient(patient);

            return ResponseEntity.ok(Map.of("message", "Patient registered successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            String specialization = request.get("specialization");
            String phone = request.get("phone");

            // Validate required fields
            if (email == null || password == null || name == null || specialization == null || phone == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }

            // Check if email already exists
            if (unifiedUserDetailsService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            // Create Doctor entity
            Doctor doctor = new Doctor();
            doctor.setName(name);
            doctor.setEmail(email);
            doctor.setPhone(phone);
            doctor.setSpecialization(specialization);
            doctor.setPassword(password); // Will be encrypted by service

            // Save doctor using unified service
            unifiedUserDetailsService.addDoctor(doctor);

            return ResponseEntity.ok(Map.of("message", "Doctor registered successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authRequest.getUsername());
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("username", authRequest.getUsername());
                
                // Get user info to determine role and name
                Optional<Doctor> doctor = doctorRepository.findByEmail(authRequest.getUsername());
                Optional<Patient> patient = patientRepository.findByEmail(authRequest.getUsername());
                
                if (doctor.isPresent()) {
                    response.put("role", "ROLE_DOCTOR");
                    response.put("name", doctor.get().getName());
                } else if (patient.isPresent()) {
                    response.put("role", "ROLE_PATIENT");
                    response.put("name", patient.get().getName());
                }
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    @GetMapping("/welcome")
    public ResponseEntity<?> welcome() {
        return ResponseEntity.ok(Map.of("message", "Welcome to Healthcare System API"));
    }
}
