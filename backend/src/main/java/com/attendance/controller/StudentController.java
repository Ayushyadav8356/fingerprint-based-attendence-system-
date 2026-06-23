package com.attendance.controller;

import com.attendance.model.*;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Student getLoggedInStudent(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Error: Logged-in user not found!"));
        return studentRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Error: Student profile not found!"));
    }

    @GetMapping("/profile")
    public Student getProfile(Principal principal) {
        return getLoggedInStudent(principal);
    }

    @GetMapping("/attendance")
    public List<Attendance> getMyAttendance(Principal principal) {
        Student student = getLoggedInStudent(principal);
        return attendanceRepository.findByStudentId(student.getId());
    }
}
