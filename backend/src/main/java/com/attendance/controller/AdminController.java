package com.attendance.controller;

import com.attendance.model.*;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/teachers")
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/classes")
    public List<Clazz> getAllClasses() {
        return clazzRepository.findAll();
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestParam String className, @RequestParam String subject, @RequestParam Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Teacher not found!"));
        
        Clazz clazz = new Clazz(className, subject, teacher);
        Clazz savedClass = clazzRepository.save(clazz);
        return ResponseEntity.ok(savedClass);
    }

    @PostMapping("/classes/{classId}/enroll/{studentId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Class not found!"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Student not found!"));

        clazz.getStudents().add(student);
        clazzRepository.save(clazz);
        return ResponseEntity.ok("Student enrolled successfully!");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body("Error: User not found!");
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok("User deleted successfully!");
    }
}
