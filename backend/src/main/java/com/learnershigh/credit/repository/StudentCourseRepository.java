package com.learnershigh.credit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.StudentCourse;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

	List<StudentCourse> findByStudentIdAndGradeYearAndTerm(String studentId, Integer gradeYear, Integer term);

}
