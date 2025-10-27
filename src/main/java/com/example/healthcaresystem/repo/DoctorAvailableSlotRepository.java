package com.example.healthcaresystem.repo;

import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.DoctorAvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorAvailableSlotRepository extends JpaRepository<DoctorAvailableSlot, Long> {
    
    List<DoctorAvailableSlot> findByDoctorOrderByAvailableTime(Doctor doctor);
    
    List<DoctorAvailableSlot> findByDoctorAndAvailableTimeAfter(Doctor doctor, LocalDateTime after);
    
    boolean existsByDoctorAndAvailableTime(Doctor doctor, LocalDateTime availableTime);
}
