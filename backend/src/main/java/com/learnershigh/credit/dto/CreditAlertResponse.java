package com.learnershigh.credit.dto;

// CreditSummaryResponse의 alert. 수강 중인 과목만 센다(api-contract.md 4.1)
public record CreditAlertResponse(Integer riskCount, Integer cautionCount) {
}
