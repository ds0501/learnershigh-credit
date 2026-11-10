package com.learnershigh.credit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

	List<Assessment> findByStudentCourseIdIn(Collection<Long> studentCourseIds);

	// 평가 목록은 등록 순서(id 오름차순)로 돌려준다 (api-contract.md 4.6 "평가 목록(등록 순서)")
	List<Assessment> findByStudentCourseIdOrderByIdAsc(Long studentCourseId);

	// 과목 삭제 시 평가도 함께 지운다 (api-contract.md 4.8)
	void deleteByStudentCourseId(Long studentCourseId);

}
