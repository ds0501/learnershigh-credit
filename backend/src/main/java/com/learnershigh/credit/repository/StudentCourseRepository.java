package com.learnershigh.credit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.StudentCourse;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

	// 등록 순서(id 오름차순)로 돌려준다 (api-contract.md 4.2 "과목 순서: 등록 순서")
	List<StudentCourse> findByStudentIdAndGradeYearAndTermOrderByIdAsc(String studentId, Integer gradeYear, Integer term);

	List<StudentCourse> findByStudentIdOrderByIdAsc(String studentId);

}
