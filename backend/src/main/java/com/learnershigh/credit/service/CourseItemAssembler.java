package com.learnershigh.credit.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.learnershigh.credit.dto.AssessmentResponse;
import com.learnershigh.credit.dto.CourseDetailResponse;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;

// StudentCourse + 평가 목록을 api-contract.md 3장 CourseItem·CourseDetail 모양으로 조립한다
// (GET courses, GET required-courses, GET courses/{courseId}, PATCH courses/{courseId}에서 공용으로 쓴다)
@Service
public class CourseItemAssembler {

	private final CourseCatalogService catalogService;
	private final CreditCalculationService calculationService;

	public CourseItemAssembler(CourseCatalogService catalogService, CreditCalculationService calculationService) {
		this.catalogService = catalogService;
		this.calculationService = calculationService;
	}

	public CourseItemResponse toItem(StudentCourse course, List<Assessment> assessments) {
		CourseEvaluation evaluation = calculationService.evaluate(course, assessments);
		return new CourseItemResponse(
				String.valueOf(course.getId()),
				catalogService.resolveCourseName(course),
				course.getCourseType().name(),
				course.getCredits(),
				course.getStatus().name(),
				evaluation.achievementRate(),
				evaluation.riskLevel().name(),
				evaluation.requiredAverage());
	}

	// 과목 상세 (api-contract.md 4.6). 평가는 등록 순서(id 오름차순)로 담는다
	public CourseDetailResponse toDetail(StudentCourse course, List<Assessment> assessments) {
		List<AssessmentResponse> assessmentResponses = assessments.stream()
				.sorted(Comparator.comparing(Assessment::getId))
				.map(AssessmentResponse::from)
				.toList();
		return new CourseDetailResponse(
				toItem(course, assessments),
				course.getGradeYear(),
				course.getTerm(),
				course.getWrittenWeight(),
				course.getPerformanceWeight(),
				assessmentResponses);
	}

}
