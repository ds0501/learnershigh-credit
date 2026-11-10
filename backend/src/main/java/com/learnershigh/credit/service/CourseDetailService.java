package com.learnershigh.credit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.learnershigh.credit.dto.CourseDetailResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;

// 과목 상세 조회 (api-contract.md 4.6). 점수 입력 패널이 쓴다
@Service
public class CourseDetailService {

	private final StudentCourseFinder courseFinder;
	private final AssessmentRepository assessmentRepository;
	private final CourseItemAssembler assembler;

	public CourseDetailService(
			StudentCourseFinder courseFinder, AssessmentRepository assessmentRepository, CourseItemAssembler assembler) {
		this.courseFinder = courseFinder;
		this.assessmentRepository = assessmentRepository;
		this.assembler = assembler;
	}

	public CourseDetailResponse getDetail(String courseId) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		List<Assessment> assessments = assessmentRepository.findByStudentCourseIdOrderByIdAsc(course.getId());
		return assembler.toDetail(course, assessments);
	}

}
