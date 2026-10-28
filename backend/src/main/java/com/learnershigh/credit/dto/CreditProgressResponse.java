package com.learnershigh.credit.dto;

// CreditSummaryResponse의 subjectCredits, creativeCredits (api-contract.md 3장 CreditProgress)
public record CreditProgressResponse(Integer earned, Integer required) {
}
