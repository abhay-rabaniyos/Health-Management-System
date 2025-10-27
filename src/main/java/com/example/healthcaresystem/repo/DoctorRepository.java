package com.example.healthcaresystem.repo;

import com.example.healthcaresystem.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByEmail(String email);
    
    Page<Doctor> findBySpecializationContainingIgnoreCase(String specialization, Pageable pageable);
    
    boolean existsByEmail(String email);
}
