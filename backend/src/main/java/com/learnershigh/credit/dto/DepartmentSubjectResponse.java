package com.learnershigh.credit.dto;

// GET /api/student/credit/departments/{departmentId}의 subjects 항목 하나
public record DepartmentSubjectResponse(
		String catalogId,
		String name,
		String courseType,
		Integer requiredUniversityCount,
		Integer totalUniversityCount,
		Double ratioPercent,
		boolean fullCommon,
		String designationType,
		boolean eduofficeListed,
		String requiredUniversities,
		boolean alreadyRegistered) {
}
