package com.learnershigh.credit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;

// StudentCourse + 평가 목록을 api-contract.md 3장 CourseItem 모양으로 조립한다 (GET courses, GET required-courses에서 공용으로 쓴다)
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

}
