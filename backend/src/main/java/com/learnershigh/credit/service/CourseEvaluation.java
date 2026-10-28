package com.learnershigh.credit.service;

import com.learnershigh.credit.dto.RiskLevel;

// 과목 하나의 성취율, 출석률, 위험 단계, 필요 평균 계산 결과. 저장하지 않는다(CLAUDE.md).
public record CourseEvaluation(Double achievementRate, double attendanceRate, RiskLevel riskLevel, Double requiredAverage) {
}
