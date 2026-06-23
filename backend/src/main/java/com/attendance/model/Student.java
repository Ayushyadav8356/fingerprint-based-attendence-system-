package com.attendance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "roll_number", nullable = false, unique = true, length = 50)
    private String rollNumber;

    @Column(name = "fingerprint_id", unique = true)
    private Integer fingerprintId;

    @Column(name = "rfid_uid", unique = true, length = 50)
    private String rfidUid;

    public Student() {}

    public Student(User user, String rollNumber, Integer fingerprintId, String rfidUid) {
        this.user = user;
        this.rollNumber = rollNumber;
        this.fingerprintId = fingerprintId;
        this.rfidUid = rfidUid;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public Integer getFingerprintId() { return fingerprintId; }
    public void setFingerprintId(Integer fingerprintId) { this.fingerprintId = fingerprintId; }

    public String getRfidUid() { return rfidUid; }
    public void setRfidUid(String rfidUid) { this.rfidUid = rfidUid; }
}
