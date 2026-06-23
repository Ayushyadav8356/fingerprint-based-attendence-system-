package com.attendance.repository;

import com.attendance.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByFingerprintId(Integer fingerprintId);
    Optional<Student> findByRfidUid(String rfidUid);
    boolean existsByRollNumber(String rollNumber);
}
