package com.learnershigh.credit.dto;

import com.learnershigh.credit.entity.Assessment;

// 평가 응답 (api-contract.md 3장 Assessment). taskId는 이번 단계는 항상 null
public record AssessmentResponse(
		String id,
		String kind,
		String title,
		Integer weightPercent,
		Integer maxScore,
		Integer score,
		String evaluatedOn,
		String taskId) {

	public static AssessmentResponse from(Assessment assessment) {
		return new AssessmentResponse(
				String.valueOf(assessment.getId()),
				assessment.getKind(),
				assessment.getTitle(),
				assessment.getWeightPercent(),
				assessment.getMaxScore(),
				assessment.getScore(),
				assessment.getEvaluatedOn().toString(),
				assessment.getTaskId());
	}

}
