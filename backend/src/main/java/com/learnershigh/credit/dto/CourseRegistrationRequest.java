package com.learnershigh.credit.dto;

// POST /api/student/credit/courses 요청 본문 (api-contract.md 4.5)
public record CourseRegistrationRequest(
		Integer gradeYear,
		Integer term,
		String catalogId,
		String customName,
		String courseType,
		Integer credits,
		String status,
		Integer writtenWeight,
		Integer performanceWeight,
		String gradeDisplay) {
}
