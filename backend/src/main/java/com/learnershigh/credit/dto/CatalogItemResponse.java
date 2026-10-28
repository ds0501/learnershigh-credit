package com.learnershigh.credit.dto;

import com.learnershigh.credit.entity.CourseCatalog;

// GET /api/student/credit/catalog?query= 응답 항목 (api-contract.md 4.4). 학점은 포함하지 않는다.
public record CatalogItemResponse(String id, String name, String subjectGroup, String courseType) {

	public static CatalogItemResponse from(CourseCatalog catalog) {
		return new CatalogItemResponse(
				String.valueOf(catalog.getId()),
				catalog.getName(),
				catalog.getSubjectGroup(),
				catalog.getCourseType().name());
	}

}
