package com.attendance.service;

import com.attendance.dto.RegisterRequest;
import com.attendance.model.Student;
import com.attendance.model.Teacher;
import com.attendance.model.User;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.TeacherRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        if ("ROLE_STUDENT".equals(request.getRole()) && request.getRollNumber() != null) {
            if (studentRepository.existsByRollNumber(request.getRollNumber())) {
                throw new IllegalArgumentException("Error: Roll number is already registered!");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        user = userRepository.save(user);

        if ("ROLE_TEACHER".equals(request.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            teacher.setDepartment(request.getDepartment() != null ? request.getDepartment() : "General");
            teacherRepository.save(teacher);
        } else if ("ROLE_STUDENT".equals(request.getRole())) {
            Student student = new Student();
            student.setUser(user);
            student.setRollNumber(request.getRollNumber() != null ? request.getRollNumber() : "CS-" + System.currentTimeMillis());
            student.setFingerprintId(request.getFingerprintId());
            student.setRfidUid(request.getRfidUid());
            studentRepository.save(student);
        }

        return user;
    }
}
