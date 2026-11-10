package com.learnershigh.credit.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

// 과목 상세 응답 (api-contract.md 4.6 CourseDetail = CourseItem + 학년/학기/반영비율/평가 목록)
public class CourseDetailResponse {

	private final CourseItemResponse item;
	private final Integer gradeYear;
	private final Integer term;
	private final Integer writtenWeight;
	private final Integer performanceWeight;
	private final List<AssessmentResponse> assessments;

	public CourseDetailResponse(
			CourseItemResponse item,
			Integer gradeYear,
			Integer term,
			Integer writtenWeight,
			Integer performanceWeight,
			List<AssessmentResponse> assessments) {
		this.item = item;
		this.gradeYear = gradeYear;
		this.term = term;
		this.writtenWeight = writtenWeight;
		this.performanceWeight = performanceWeight;
		this.assessments = assessments;
	}

	@JsonUnwrapped
	public CourseItemResponse getItem() {
		return item;
	}

	public Integer getGradeYear() {
		return gradeYear;
	}

	public Integer getTerm() {
		return term;
	}

	public Integer getWrittenWeight() {
		return writtenWeight;
	}

	public Integer getPerformanceWeight() {
		return performanceWeight;
	}

	public List<AssessmentResponse> getAssessments() {
		return assessments;
	}

}
