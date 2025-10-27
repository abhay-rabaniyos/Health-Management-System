package com.example.healthcaresystem.repo;

import com.example.healthcaresystem.model.Appointment;
import com.example.healthcaresystem.model.Doctor;
import com.example.healthcaresystem.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatientOrderByAppointmentTimeDesc(Patient patient);
    
    List<Appointment> findByDoctorOrderByAppointmentTimeDesc(Doctor doctor);
    
    boolean existsByDoctorAndAppointmentTimeAndStatus(Doctor doctor, LocalDateTime appointmentTime, Appointment.Status status);
    
    List<Appointment> findByDoctorAndAppointmentTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
}
