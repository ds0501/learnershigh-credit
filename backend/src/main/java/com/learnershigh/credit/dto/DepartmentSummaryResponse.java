package com.learnershigh.credit.dto;

import com.learnershigh.credit.entity.Department;

// GET /api/student/credit/departments?query= 의 검색 결과 항목
public record DepartmentSummaryResponse(String id, String name, String field, Integer totalUniversityCount, boolean lowSample) {

	public static DepartmentSummaryResponse from(Department department) {
		return new DepartmentSummaryResponse(
				String.valueOf(department.getId()),
				department.getName(),
				department.getField(),
				department.getTotalUniversityCount(),
				Boolean.TRUE.equals(department.getLowSample()));
	}

}
