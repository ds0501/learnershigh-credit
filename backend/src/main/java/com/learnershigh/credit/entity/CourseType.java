package com.learnershigh.credit.entity;

// 과목 유형. SPECIALIZED(전문교과)는 credit_course_catalog에만 쓰인다(PRD 12장 미정 — 프론트는 아직 대응 안 됨).
public enum CourseType {
	COMMON,
	GENERAL,
	CAREER,
	FUSION,
	CREATIVE,
	SPECIALIZED
}
