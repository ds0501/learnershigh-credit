package com.learnershigh.credit.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 이수 상태 변경·과목 삭제 (api-contract.md 4.7, 4.8)
@Service
public class CourseManagementService {

	private final StudentCourseFinder courseFinder;
	private final StudentCourseRepository studentCourseRepository;
	private final AssessmentRepository assessmentRepository;
	private final CourseItemAssembler assembler;

	public CourseManagementService(
			StudentCourseFinder courseFinder,
			StudentCourseRepository studentCourseRepository,
			AssessmentRepository assessmentRepository,
			CourseItemAssembler assembler) {
		this.courseFinder = courseFinder;
		this.studentCourseRepository = studentCourseRepository;
		this.assessmentRepository = assessmentRepository;
		this.assembler = assembler;
	}

	// 없는 과목이 404이므로 먼저 찾고, 그 다음 상태 값을 검증한다 (api-contract.md 4.7)
	public CourseItemResponse changeStatus(String courseId, String status) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		if (!"IN_PROGRESS".equals(status) && !"COMPLETED".equals(status)) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("status", "이수 상태를 선택하세요."));
		}
		course.changeStatus(CourseStatus.valueOf(status));
		StudentCourse saved = studentCourseRepository.save(course);
		List<Assessment> assessments = assessmentRepository.findByStudentCourseIdIn(List.of(saved.getId()));
		return assembler.toItem(saved, assessments);
	}

	// 과목과 그 과목의 평가를 함께 지운다 (api-contract.md 4.8)
	// deleteByStudentCourseId는 파생 삭제 쿼리라 트랜잭션이 없으면 실행할 수 없다(TransactionRequiredException)
	@Transactional
	public void delete(String courseId) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		assessmentRepository.deleteByStudentCourseId(course.getId());
		studentCourseRepository.delete(course);
	}

}
