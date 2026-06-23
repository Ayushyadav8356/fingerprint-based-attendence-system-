package com.attendance.repository;

import com.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByClazzId(Long clazzId);
    List<Attendance> findByClazzTeacherId(Long teacherId);
    List<Attendance> findByStudentIdAndClazzIdAndTimestampBetween(Long studentId, Long classId, LocalDateTime start, LocalDateTime end);
}
