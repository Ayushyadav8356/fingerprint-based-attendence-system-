package com.attendance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private Clazz clazz;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    private String status; // 'PRESENT', 'LATE', 'ABSENT'

    @Column(nullable = false, length = 20)
    private String method; // 'FINGERPRINT', 'RFID', 'MANUAL'

    public Attendance() {}

    public Attendance(Student student, Clazz clazz, String status, String method) {
        this.student = student;
        this.clazz = clazz;
        this.status = status;
        this.method = method;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Clazz getClazz() { return clazz; }
    public void setClazz(Clazz clazz) { this.clazz = clazz; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
