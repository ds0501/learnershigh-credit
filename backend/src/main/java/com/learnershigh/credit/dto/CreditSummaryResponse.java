package com.learnershigh.credit.dto;

// GET /api/student/credit/summary 응답 (api-contract.md 4.1)
public record CreditSummaryResponse(
		Integer graduationCredits,
		Integer completedCredits,
		Integer inProgressCredits,
		Integer remainingCredits,
		CreditProgressResponse subjectCredits,
		CreditProgressResponse creativeCredits,
		CreditAlertResponse alert) {
}
