package com.example.healthcaresystem.service;

import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.Patient;
import com.example.healthcaresystem.repo.DoctorRepository;
import com.example.healthcaresystem.repo.PatientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnifiedUserDetailsService implements UserDetailsService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UnifiedUserDetailsService(DoctorRepository doctorRepository, 
                                   PatientRepository patientRepository,
                                   PasswordEncoder encoder) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find as a doctor
        Optional<Doctor> doctor = doctorRepository.findByEmail(username);
        if (doctor.isPresent()) {
            Doctor doc = doctor.get();
            List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_DOCTOR"));
            return new User(doc.getEmail(), doc.getPassword(), authorities);
        }

        // Then try to find as a patient
        Optional<Patient> patient = patientRepository.findByEmail(username);
        if (patient.isPresent()) {
            Patient pat = patient.get();
            List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_PATIENT"));
            return new User(pat.getEmail(), pat.getPassword(), authorities);
        }

        throw new UsernameNotFoundException("User not found with email: " + username);
    }

    public String addDoctor(Doctor doctor) {
        // Encrypt password before saving
        doctor.setPassword(encoder.encode(doctor.getPassword()));
        doctorRepository.save(doctor);
        return "Doctor added successfully!";
    }

    public String addPatient(Patient patient) {
        // Encrypt password before saving
        patient.setPassword(encoder.encode(patient.getPassword()));
        patientRepository.save(patient);
        return "Patient added successfully!";
    }

    public boolean emailExists(String email) {
        return doctorRepository.findByEmail(email).isPresent() || 
               patientRepository.findByEmail(email).isPresent();
    }
}
