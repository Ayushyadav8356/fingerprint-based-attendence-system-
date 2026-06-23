package com.attendance.repository;

import com.attendance.model.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long> {
    List<Clazz> findByTeacherId(Long teacherId);

    @Query("SELECT c FROM Clazz c JOIN c.students s WHERE s.id = :studentId")
    List<Clazz> findClassesByStudentId(@Param("studentId") Long studentId);
}

