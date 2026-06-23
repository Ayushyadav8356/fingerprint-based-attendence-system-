package com.attendance.controller;

import com.attendance.model.*;
import com.attendance.repository.*;
import com.attendance.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CsvService csvService;

    private Teacher getLoggedInTeacher(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Error: Logged-in user not found!"));
        return teacherRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Error: Teacher details not found!"));
    }

    @GetMapping("/classes")
    public List<Clazz> getMyClasses(Principal principal) {
        Teacher teacher = getLoggedInTeacher(principal);
        return clazzRepository.findByTeacherId(teacher.getId());
    }

    @GetMapping("/classes/{classId}")
    public Clazz getClassDetails(@PathVariable Long classId, Principal principal) {
        Teacher teacher = getLoggedInTeacher(principal);
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Class not found!"));
        
        if (!clazz.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Error: Access Denied! You do not teach this class.");
        }
        
        // Trigger initialization of students Set before return (for serialization)
        clazz.getStudents().size();
        return clazz;
    }

    @GetMapping("/attendance/logs")
    public List<Attendance> getMyAttendanceLogs(Principal principal) {
        Teacher teacher = getLoggedInTeacher(principal);
        return attendanceRepository.findByClazzTeacherId(teacher.getId());
    }

    @PostMapping("/attendance/mark")
    public ResponseEntity<?> markManualAttendance(
            @RequestParam Long studentId,
            @RequestParam Long classId,
            @RequestParam String status,
            Principal principal) {
        
        Teacher teacher = getLoggedInTeacher(principal);
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Class not found!"));
        
        if (!clazz.getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body("Error: Access Denied! You do not teach this class.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Student not found!"));

        // Check if student is enrolled in this class
        boolean isEnrolled = clazz.getStudents().stream().anyMatch(s -> s.getId().equals(studentId));
        if (!isEnrolled) {
            return ResponseEntity.badRequest().body("Error: Student is not enrolled in this class.");
        }

        // Prevent duplicates on the same calendar day
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Attendance> existingLogs = attendanceRepository.findByStudentIdAndClazzIdAndTimestampBetween(
                studentId, classId, startOfDay, endOfDay);
        
        if (!existingLogs.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Attendance already recorded for this student today.");
        }

        Attendance attendance = new Attendance(student, clazz, status.toUpperCase(), "MANUAL");
        attendanceRepository.save(attendance);

        return ResponseEntity.ok("Attendance marked successfully as " + status);
    }

    @GetMapping("/attendance/export")
    public ResponseEntity<InputStreamResource> exportAttendance(Principal principal) {
        Teacher teacher = getLoggedInTeacher(principal);
        List<Attendance> logs = attendanceRepository.findByClazzTeacherId(teacher.getId());
        
        ByteArrayInputStream in = csvService.generateAttendanceCsv(logs);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=attendance_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(new InputStreamResource(in));
    }
}
