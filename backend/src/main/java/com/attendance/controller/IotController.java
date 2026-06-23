package com.attendance.controller;

import com.attendance.dto.IotScanRequest;
import com.attendance.dto.IotScanResponse;
import com.attendance.model.*;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/iot")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IotController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Value("${iot.device.token}")
    private String configuredDeviceToken;

    @PostMapping("/scan")
    public ResponseEntity<?> handleIotScan(
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestBody IotScanRequest scanRequest) {

        // 1. Verify Security Token
        if (deviceToken == null || !deviceToken.equals(configuredDeviceToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new IotScanResponse("error", "Access Denied", "Invalid Device Token"));
        }

        // 2. Identify Student
        Optional<Student> studentOpt = Optional.empty();
        String scanMethod = "";

        if (scanRequest.getFingerprintId() != null) {
            studentOpt = studentRepository.findByFingerprintId(scanRequest.getFingerprintId());
            scanMethod = "FINGERPRINT";
        } else if (scanRequest.getRfidUid() != null && !scanRequest.getRfidUid().trim().isEmpty()) {
            studentOpt = studentRepository.findByRfidUid(scanRequest.getRfidUid());
            scanMethod = "RFID";
        }

        if (studentOpt.isEmpty()) {
            return ResponseEntity.ok(new IotScanResponse("error", "Unknown", "Biometric ID Not Found"));
        }

        Student student = studentOpt.get();
        String studentName = student.getUser().getFullName();

        // 3. Resolve Class
        Clazz targetClass = null;
        if (classId != null) {
            targetClass = clazzRepository.findById(classId).orElse(null);
        }

        if (targetClass == null) {
            // Fallback: Find classes student is enrolled in
            List<Clazz> enrolledClasses = clazzRepository.findClassesByStudentId(student.getId());
            if (enrolledClasses.isEmpty()) {
                return ResponseEntity.ok(new IotScanResponse("error", studentName, "Not enrolled in any class"));
            }
            // Auto-select the first enrolled class for demonstration
            targetClass = enrolledClasses.get(0);
        }

        // Verify student is actually enrolled in target class
        Clazz finalClazz = targetClass;
        boolean isEnrolled = clazzRepository.findClassesByStudentId(student.getId())
                .stream().anyMatch(c -> c.getId().equals(finalClazz.getId()));
        
        if (!isEnrolled) {
            return ResponseEntity.ok(new IotScanResponse("error", studentName, "Not enrolled in " + targetClass.getClassName()));
        }

        // 4. Prevent Duplicates on the Same Day
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Attendance> existingLogs = attendanceRepository.findByStudentIdAndClazzIdAndTimestampBetween(
                student.getId(), targetClass.getId(), startOfDay, endOfDay);

        if (!existingLogs.isEmpty()) {
            return ResponseEntity.ok(new IotScanResponse("success", studentName, "Already Marked for " + targetClass.getClassName()));
        }

        // 5. Determine Status (e.g. Present if scanned before 9:30 AM, else Late)
        String status = "PRESENT";
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(9, 30))) {
            status = "LATE";
        }

        // 6. Log Attendance
        Attendance attendance = new Attendance(student, targetClass, status, scanMethod);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok(new IotScanResponse("success", studentName, "Marked " + status + " for " + targetClass.getClassName()));
    }
}
