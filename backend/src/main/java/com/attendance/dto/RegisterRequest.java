package com.attendance.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String role; // 'ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT'
    private String fullName;
    private String email;

    // Teacher specific
    private String department;

    // Student specific
    private String rollNumber;
    private Integer fingerprintId;
    private String rfidUid;

    public RegisterRequest() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public Integer getFingerprintId() { return fingerprintId; }
    public void setFingerprintId(Integer fingerprintId) { this.fingerprintId = fingerprintId; }

    public String getRfidUid() { return rfidUid; }
    public void setRfidUid(String rfidUid) { this.rfidUid = rfidUid; }
}
