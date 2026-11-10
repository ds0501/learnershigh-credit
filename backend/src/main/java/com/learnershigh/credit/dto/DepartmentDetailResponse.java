package com.learnershigh.credit.dto;

import java.util.List;

// GET /api/student/credit/departments/{departmentId} 응답
public record DepartmentDetailResponse(
		String id,
		String name,
		String field,
		Integer totalUniversityCount,
		boolean lowSample,
		String generalElectiveText,
		String careerElectiveText,
		String fusionElectiveText,
		List<DepartmentSubjectResponse> subjects) {
}
