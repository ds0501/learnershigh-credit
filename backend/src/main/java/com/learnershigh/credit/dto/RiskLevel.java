package com.learnershigh.credit.dto;

// 이수 위험 단계 (api-contract.md 3장). 계산 결과이고 저장하지 않는다.
public enum RiskLevel {
	NO_DATA,
	NORMAL,
	CAUTION,
	RISK
}
