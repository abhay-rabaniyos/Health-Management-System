package com.example.healthcaresystem.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class DoctorAvailableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime availableTime;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    // Constructors
    public DoctorAvailableSlot() {}

    public DoctorAvailableSlot(Doctor doctor, LocalDateTime availableTime) {
        this.doctor = doctor;
        this.availableTime = availableTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(LocalDateTime availableTime) {
        this.availableTime = availableTime;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}

