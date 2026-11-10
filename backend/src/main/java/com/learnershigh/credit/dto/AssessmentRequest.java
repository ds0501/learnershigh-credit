package com.learnershigh.credit.dto;

// POST .../assessments, PUT .../assessments/{assessmentId} 요청 본문 (api-contract.md 4.9, 4.10)
public record AssessmentRequest(
		String kind,
		String title,
		Integer weightPercent,
		Integer maxScore,
		Integer score,
		String evaluatedOn) {
}
