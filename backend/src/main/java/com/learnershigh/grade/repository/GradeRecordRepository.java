package com.learnershigh.grade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.grade.entity.GradeRecord;

public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

	List<GradeRecord> findByStudentIdAndGradeYearAndTermOrderByIdAsc(String studentId, Integer gradeYear, Integer term);

	Optional<GradeRecord> findByStudentIdAndGradeYearAndTermAndSubjectName(
			String studentId, Integer gradeYear, Integer term, String subjectName);

}
